package com.github.avrokotlin.avro4k

import com.github.avrokotlin.avro4k.schema.DefaultNamingStrategy
import com.github.avrokotlin.avro4k.schema.NamingStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

@ExperimentalSerializationApi
data class RecordNaming internal constructor(
    /**
     * The record name for this type to be used when creating
     * an avro record. This method takes into account type parameters and
     * annotations.
     *
     * The general format for a record name is `resolved-name__typea_typeb_typec`.
     * That is a double underscore delimits the resolved name from the start of the
     * type parameters and then each type parameter is delimited by a single underscore.
     *
     * The resolved name is the class name with any annotations applied, such
     * as @AvroName or @AvroNamespace, or @AvroErasedName, which, if present,
     * means the type parameters will not be included in the final name.
     */
    val name: String,
    /**
     * The namespace for this type to be used when creating
     * an avro record. This method takes into account @AvroNamespace.
     */
    val namespace: String?
) {
   val fullName : String by lazy { 
      namespace?.let { "$namespace.$name" } ?: name
   }

   companion object {
      operator fun invoke(name: String, annotations: List<Annotation>, namingStrategy: NamingStrategy): RecordNaming {
         val className = name
            .replace(".<init>", "")
            .replace(".<anonymous>", "")
         val annotationExtractor = AnnotationExtractor(annotations)
         val namespace = annotationExtractor.namespace() ?: className.split('.').dropLast(1).joinToString(".")
         val avroName = annotationExtractor.name() ?: className.split('.').last()
         return RecordNaming(
            name = namingStrategy.to(avroName),
            namespace = namespace
         )
      }

      operator fun invoke(descriptor: SerialDescriptor, namingStrategy: NamingStrategy): RecordNaming = RecordNaming(
         if (descriptor.isNullable) descriptor.serialName.removeSuffix("?") else descriptor.serialName,
         descriptor.annotations,
         namingStrategy
      )

      operator fun invoke(descriptor: SerialDescriptor, index: Int, namingStrategy: NamingStrategy): RecordNaming =
         RecordNaming(
            descriptor.getElementName(index),
            descriptor.getElementAnnotations(index),
            namingStrategy
         )

      operator fun invoke(name: String, annotations: List<Annotation>): RecordNaming =
         invoke(name, annotations, DefaultNamingStrategy)

      operator fun invoke(descriptor: SerialDescriptor): RecordNaming = invoke(descriptor, DefaultNamingStrategy)

   }
}

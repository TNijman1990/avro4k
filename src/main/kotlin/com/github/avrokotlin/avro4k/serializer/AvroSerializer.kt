package com.github.avrokotlin.avro4k.serializer

import com.github.avrokotlin.avro4k.decoder.ExtendedDecoder
import com.github.avrokotlin.avro4k.decoder.FieldDecoder
import com.github.avrokotlin.avro4k.encoder.avro.ExtendedEncoder
import com.github.avrokotlin.avro4k.encoder.avro.SchemaBasedEncoder
import com.github.avrokotlin.avro4k.schema.extractNonNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.apache.avro.Schema

abstract class AvroSerializer<T> : KSerializer<T> {

   final override fun serialize(encoder: Encoder, value: T) {
      val schema = (encoder as SchemaBasedEncoder).fieldSchema()
      // we may be encoding a nullable schema
      val subschema = when (schema.type) {
         Schema.Type.UNION -> schema.extractNonNull()
         else -> schema
      }
      encodeAvroValue(subschema, encoder as ExtendedEncoder, value)
   }

   final override fun deserialize(decoder: Decoder): T {
      val schema = (decoder as FieldDecoder).fieldSchema()
//      // we may be coming from a nullable schema aka a union
//      val subschema = when (schema.type) {
//         Schema.Type.UNION -> schema.extractNonNull()
//         else -> schema
//      }
      return decodeAvroValue(schema, decoder)
   }

   abstract fun encodeAvroValue(schema: Schema, encoder: ExtendedEncoder, obj: T)

   abstract fun decodeAvroValue(schema: Schema, decoder: ExtendedDecoder): T
}


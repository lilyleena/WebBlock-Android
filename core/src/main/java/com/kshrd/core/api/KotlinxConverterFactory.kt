package com.kshrd.core.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

/**
 * Retrofit converter for kotlinx.serialization, so the v2 wire types are checked against Kotlin's nullability
 * (Gson would put `null` into a non-null field). `Unit` and `ResponseBody` answers are handled by Retrofit itself.
 */
class KotlinxConverterFactory(
    private val json: Json = ApiJson,
) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val serializer = json.serializersModule.serializer(type)
        return Converter<ResponseBody, Any?> { body -> body.use { json.decodeFromString(serializer, it.string()) } }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody> {
        val serializer = json.serializersModule.serializer(type)
        return Converter<Any, RequestBody> { value -> json.encodeToString(serializer, value).toRequestBody(JSON_MEDIA_TYPE) }
    }
}

package com.jikexueyuan.mobile.address.api

import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.ResponseBody
import java.lang.reflect.Type

import retrofit.Converter

/**
 * Created by aven on 10/22/15.
 */
class StringConvertFactory : Converter.Factory() {
    override fun fromResponseBody(type: Type?, annotations: Array<Annotation>?): Converter<ResponseBody, *> {
        return StringResponseBodyConverter()
    }

    override fun toRequestBody(type: Type?, annotations: Array<Annotation>?): Converter<*, RequestBody> {
        return super.toRequestBody(type, annotations)
    }
}

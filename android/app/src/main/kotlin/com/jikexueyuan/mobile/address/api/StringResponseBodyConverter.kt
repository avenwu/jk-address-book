package com.jikexueyuan.mobile.address.api

import com.squareup.okhttp.ResponseBody

import java.io.IOException

import retrofit.Converter

/**
 * Created by aven on 10/22/15.
 */
class StringResponseBodyConverter : Converter<ResponseBody, String> {

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): String {
        return String(value.bytes(), "UTF-8")
    }
}

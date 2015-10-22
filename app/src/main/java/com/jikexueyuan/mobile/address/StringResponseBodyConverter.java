package com.jikexueyuan.mobile.address;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import retrofit.Converter;

/**
 * Created by aven on 10/22/15.
 */
public class StringResponseBodyConverter implements Converter<ResponseBody, String> {

    @Override
    public String convert(ResponseBody value) throws IOException {
        return new String(value.bytes(), "UTF-8");
    }
}

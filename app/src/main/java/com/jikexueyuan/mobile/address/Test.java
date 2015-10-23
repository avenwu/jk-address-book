package com.jikexueyuan.mobile.address;

import android.os.Parcel;
import android.os.Parcelable;

import com.jikexueyuan.mobile.address.bean.User;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aven on 10/23/15.
 */
public class Test {
//    public static void test() {
//        UtilsKt.getUserListFromCache(null, new AppService.Block<List<? extends User>>() {
//            @Override
//            public void onCallback(List<? extends User> data) {
//
//            }
//        });
//    }

    public static class User implements Serializable {
        String username;
        String email;
        String phone;
        String qq;
        String wechat;


    }
}


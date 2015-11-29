package com.jikexueyuan.mobile.address;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Chaobin Wu on 2014/11/4.
 */
public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new Authenticator(this).getIBinder();
    }
}

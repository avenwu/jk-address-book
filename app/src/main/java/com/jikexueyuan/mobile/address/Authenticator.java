package com.jikexueyuan.mobile.address;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.jikexueyuan.mobile.address.ui.LoginActivity;

/**
 * Kotlin 对AbstractAccountAuthenticator的支持有问题，可能是Service问题，也可能是重载问题
 * Created by Chaobin Wu on 2014/11/4.
 */
public class Authenticator extends AbstractAccountAuthenticator {
    final Context mContext;
    public static final String PARAM_ACCOUNT_TYPE = BuildConfig.APPLICATION_ID;
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    public static final String PARAM_USER = "user";
    public static final String PARAM_CONFIRMCREDENTIALS =
            "confirmCredentials";
    public Authenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Authenticator.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_MANAGER_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        if (!authTokenType.equals(Authenticator.PARAM_ACCOUNT_TYPE)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }
        final AccountManager manager = AccountManager.get(mContext);
        final String pwd = manager.getPassword(account);
        if (pwd != null) {
            boolean verified = false;
//            String loginResponse = null;
//            loginResponse =
            //TODO login
            verified = true;
            if (verified) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.PARAM_ACCOUNT_TYPE);
                return result;
            }
        }
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Authenticator.PARAM_USER, account.name);
        intent.putExtra(Authenticator.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}

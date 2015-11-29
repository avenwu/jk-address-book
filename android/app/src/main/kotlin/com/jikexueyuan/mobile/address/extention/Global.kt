package com.jikexueyuan.mobile.address.extention

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

/**
 * Created by aven on 10/26/15.
 */
/**
 * Toast提示
 */
public fun toast(context: Context, text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, text, duration).show()
}

public fun toast(context: Context, text: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, text, duration).show()
}

/**
 * 检查Intent是否可以被处理
 */
public fun Intent.canBeHandled(context: Context): Boolean {
    return context.getPackageManager().queryIntentActivities(this,
            PackageManager.MATCH_DEFAULT_ONLY).size > 0;
}

/**
 * 情动activity，失败的会回调block
 */
public fun Context.startActivitySafely(intent: Intent, block: ((Context) -> Unit)? = null) {
    if (intent.canBeHandled(this)) {
        startActivity(intent)
    } else {
        block?.let {
            block(this)
        }
    }
}

/**
 * 根据包名检查app是否安装了
 */
public fun Context.isPackageInstalled(packageName: String): Intent? {
    return packageManager.getLaunchIntentForPackage(packageName) ?: null
}

/**
 * 启动app
 */
public fun Context.launchApp(packageName: String): Boolean {
    val intent = isPackageInstalled(packageName)
    if (intent != null) {
        startActivity(intent)
        return true
    } else {
        return false
    }
}

/**
 * 根据包名在Google Play市场中打开软件页面
 */
public fun Context.openPlayMarket(packageName: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: android.content.ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")));
    }
}

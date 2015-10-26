package com.jikexueyuan.mobile.address.extention

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

/**
 * Created by aven on 10/26/15.
 */

public fun toast(context: Context, text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, text, duration).show()
}

public fun Intent.canBeHandled(context: Context): Boolean {
    return context.getPackageManager().queryIntentActivities(this,
            PackageManager.MATCH_DEFAULT_ONLY).size > 0;
}

public fun Context.startActivitySafely(intent: Intent, tips: CharSequence = "Intent can not be handled properly") {
    if (intent.canBeHandled(this)) {
        startActivity(intent)
    } else {
        toast(this, tips)
    }
}

public fun Context.isPackageInstalled(packageName: String): Intent? {
    return packageManager.getLaunchIntentForPackage(packageName) ?: null
}

public fun Context.launchApp(packageName: String): Boolean {
    val intent = isPackageInstalled(packageName)
    if (intent != null) {
        startActivity(intent)
        return true
    } else {
        return false
    }
}

public fun Context.openPlayMarket(packageName: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: android.content.ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")));
    }
}
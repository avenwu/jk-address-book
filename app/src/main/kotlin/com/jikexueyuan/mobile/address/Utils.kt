package com.jikexueyuan.mobile.address

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.support.v4.os.AsyncTaskCompat
import android.util.Log
import com.jikexueyuan.mobile.address.bean.User
import com.jikexueyuan.mobile.address.extention.launchApp
import com.jikexueyuan.mobile.address.extention.openPlayMarket
import com.jikexueyuan.mobile.address.extention.startActivitySafely
import com.jikexueyuan.mobile.address.extention.toast
import org.json.JSONObject
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Created by aven on 10/23/15.
 */

public fun saveUserList2Cache(context: Context, list: List<User>) {
    AsyncTaskCompat.executeParallel(object : AsyncTask<Any, Void, Void>() {
        override fun doInBackground(vararg params: Any): Void? {
            params[0]?.let {
                try {
                    var fos = (params[0] as Context).openFileOutput("user-list", Context.MODE_PRIVATE);
                    var os = ObjectOutputStream(fos);
                    os.writeObject(params[1]);
                    os.close();
                    fos.close();
                } catch(e: Exception) {
                    e.printStackTrace()
                    Log.d("File", "save user failed")
                }

            }
            return null
        }
    }, context, list)
}

public fun getUserListFromCache(context: Context, listener: (List<User>) -> Unit) {
    AsyncTaskCompat.executeParallel(object : AsyncTask<Any, Void, List<User>>() {
        override fun doInBackground(vararg params: Any?): List<User>? {
            var dataList: List<User> = emptyList()
            try {
                var fis = (params[0] as Context).openFileInput("user-list");
                var inStream = ObjectInputStream(fis);
                dataList = inStream.readObject() as List<User>;
                inStream.close();
                fis.close();
            } catch(e: Exception) {
                e.printStackTrace()
                Log.d("File", "get user cache failed")
            }
            return dataList
        }

        override fun onPostExecute(result: List<User>) {
            super.onPostExecute(result)
            listener(result)
        }
    }, context)
}

public fun json2User(json: String): User? {
    val jsonObject = JSONObject(json)
    return User(jsonObject.optString("username"), jsonObject.optString("email"),
            jsonObject.optString("phone"), jsonObject.optString("qq"), jsonObject.optString("wechat"))
}

public fun mockUserList(): List<User> {
    return listOf(User("A", "zhangsan", "zhangsan@jkxy.com"),
            User("B", "b", "b"),
            User("C", "c", "c"),
            User("D", "c", "c"),
            User("E", "c", "c"),
            User("F", "c", "c"),
            User("G", "c", "c"),
            User("H", "c", "c"),
            User("I", "c", "c"),
            User("J", "c", "c"),
            User("K", "c", "c"),
            User("L", "c", "c"),
            User("M", "c", "c"),
            User("N", "c", "c"),
            User("O", "c", "c"),
            User("P", "c", "c"))
}

public fun isNetWorkAvailable(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = manager.activeNetworkInfo
    return info != null && info.isAvailable
}

public fun makeCall(context: Context, phone: CharSequence) {
    context.startActivitySafely(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)), "无法唤起拨号面板")
}

public fun sendEmail(context: Context, email: CharSequence) {
    context.startActivitySafely(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")), "没有邮件客户端")
}

public fun openQQ(context: Context, qq: CharSequence) {
    val packageName = "com.tencent.mobileqq"
    if (context.launchApp(packageName)) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.primaryClip = ClipData.newPlainText("QQ号码", qq)
        toast(context, "QQ号已复制，可长按粘贴在QQ搜索栏内")
    } else {
        toast(context, "壮士，没有检测到QQ客户端")
        context.openPlayMarket(packageName)
    }
}

public fun openWeChat(context: Context, weChat: CharSequence) {
    val packageName = "com.tencent.mm"
    if (context.launchApp(packageName)) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.primaryClip = ClipData.newPlainText("微信号码", weChat)
        toast(context, "微信号已复制，可长按粘贴在微信搜索栏内")
    } else {
        toast(context, "壮士，没有检测到微信客户端")
        context.openPlayMarket(packageName)
    }
}

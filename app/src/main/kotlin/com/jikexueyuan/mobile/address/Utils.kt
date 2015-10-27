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
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination
import org.json.JSONObject
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.test.assertEquals

/**
 * Created by aven on 10/23/15.
 */

public fun saveUserList2Cache(context: Context, list: List<User>, filter: ((List<User>) -> List<User>)? = null) {
    AsyncTaskCompat.executeParallel(object : AsyncTask<Any, Void, Void>() {
        override fun doInBackground(vararg params: Any): Void? {
            params[0].let {
                try {
                    var fos = (params[0] as Context).openFileOutput("user-list", Context.MODE_PRIVATE);
                    var os = ObjectOutputStream(fos);
                    var filterData: List<User>? = null
                    filter?.let {
                        filterData = emptyList()
                        filterData = filter(params[1] as List<User>)
                    }
                    os.writeObject(filterData ?: params[1]);
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

public fun getUserListFromCache(context: Context, listener: (List<User>) -> Unit, filter: ((List<User>) -> List<User>)? = null) {
    AsyncTaskCompat.executeParallel(object : AsyncTask<Any, Void, List<User>>() {
        override fun doInBackground(vararg params: Any?): List<User>? {
            var dataList: List<User>? = emptyList()
            try {
                var fis = (params[0] as Context).openFileInput("user-list");
                var inStream = ObjectInputStream(fis);
                var data = inStream.readObject() as List<User>;
                inStream.close();
                fis.close();
                filter?.let {
                    dataList = filter(data)
                }
                if (dataList == null || (dataList?.size == 0 && data.size > 0)) {
                    dataList = data
                }
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
    context.startActivitySafely(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)), {
        toast(it, "无法唤起拨号面板")
    })
}

public fun sendEmail(context: Context, email: CharSequence) {
    context.startActivitySafely(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")), {
        toast(it, "没有邮件客户端")
    })
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

public fun skipSameUserFilter(oldData: List<User>): List<User> {
    var dataList = emptyList<User>()
    Log.w("Filter", "过滤重复号码")
    var phoneSet = emptySet<String>()
    for (user in oldData) {
        if (phoneSet.contains(user.phone)) {
            Log.w("Filter", "跳过重复号：${user.json()}")
            continue
        }
        phoneSet.plus(user.phone)
        dataList.plus(user.copy())
    }
    return dataList
}

public fun export2VCard(context: Context, user: List<User>, block: (File?) -> Unit) {
    AsyncTaskCompat.executeParallel(object : AsyncTask<Any, Void, File>() {
        override fun doInBackground(vararg params: Any?): File? {
            params[0]?.let {
                var file = params[0] as File
                var userInVCardFormat = user2VCardFormatString(params[1] as List<User>)
                file.writeText(userInVCardFormat)
                return file
            }
            return null
        }

        override fun onPostExecute(result: File?) {
            super.onPostExecute(result)
            block(result)
        }
    }, File(context.externalCacheDir, "极客学院联系人.vcf"), user)
}

fun user2VCardFormatString(users: List<User>): String {
    var data = StringBuilder()
    for (user in users) {
        data.append("BEGIN:VCARD\n" +
                "VERSION:2.1\n" +
                "FN:${user.username}\n" +
                "ORG:极客学院\n" +
                "TEL;TYPE=cell:${user.phone}\n" +
                "EMAIL:${user.email}\n" +
                "X-QQ:${user.qq}\n" +
                "X-WECHAT:${user.wechat}\n" +
                "END:VCARD\n")
    }
    return data.toString()
}

public fun chinese2Pinyin(chinese: String): String {
    val pinyingBuffer = StringBuffer()
    val defaultFormat = HanyuPinyinOutputFormat()
    defaultFormat.caseType = HanyuPinyinCaseType.UPPERCASE
    defaultFormat.toneType = HanyuPinyinToneType.WITHOUT_TONE
    for (c in chinese.toCharArray()) {
        if (c.toInt() > 128) {
            try {
                var array = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)
                array?.let {
                    pinyingBuffer.append(array[0])
                }
            } catch(e: Throwable) {
                e.printStackTrace()
            }
        } else {
            pinyingBuffer.append(c)
        }
    }
    return pinyingBuffer.toString()
}

fun sortUserList(raw: List<User>, block: (List<User>) -> Unit) {
    AsyncTaskCompat.executeParallel(object : AsyncTask<List<User>, Void, List<User>>() {
        override fun doInBackground(vararg params: List<User>?): List<User>? {
            return params[0]?.sortedWith(Comparator { left, right ->
                left.pinying.compareTo(right.pinying)
            })
        }

        override fun onPostExecute(result: List<User>?) {
            super.onPostExecute(result)
            block(result ?: emptyList())
        }
    }, raw)
}

public fun updateLoginTimestamp(context: Context, mills: Long) {
    var sp = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
    sp.edit().putLong("login_timestamp", mills).apply()
}

public fun getLoginTimestamp(context: Context): Long {
    return context.getSharedPreferences("user_info", Context.MODE_PRIVATE).getLong("login_timestamp", 0)
}
package com.jikexueyuan.mobile.address.api

import android.os.AsyncTask
import android.support.v4.os.AsyncTaskCompat
import android.text.TextUtils
import com.jikexueyuan.mobile.address.bean.AddressBook
import com.jikexueyuan.mobile.address.bean.User
import com.jikexueyuan.mobile.address.chinese2Pinyin
import com.squareup.okhttp.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import retrofit.Retrofit
import java.io.IOException
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by aven on 10/22/15.
 */
object AppService {
    private val api: API
    public val PAGE_COUNT = 20

    init {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        CookieHandler.setDefault(cookieManager)
        val client = OkHttpClient()
        client.setConnectTimeout((client.connectTimeout * 2).toLong(), TimeUnit.MILLISECONDS)
        client.setReadTimeout((client.readTimeout * 2).toLong(), TimeUnit.MILLISECONDS)
        api = Retrofit.Builder().baseUrl("http://work.eoemobile.com")
                .addConverterFactory(StringConvertFactory())
                .client(client)
                .build()
                .create(API::class.java)
    }

    fun getAddressBookByPage(pageIndex: Int, listener: (AddressBook) -> Unit): AsyncTask<*, *, *> {
        return AsyncTaskCompat.executeParallel(object : AsyncTask<Int, Void, AddressBook>() {
            override fun doInBackground(vararg params: Int?): AddressBook? {
                var page = params[0] ?: 0
                var result: AddressBook
                try {
                    val call = api.addressBooks(page, PAGE_COUNT)
                    val response = call.execute()
                    //User Info
                    val document = Jsoup.parse(response.body(), "UTF-8")
                    val userTable = document.select("div table tbody tr")

                    val userList = ArrayList<User>()
                    for (index in userTable.indices) {
                        userList.add(parseUser(userTable, index))
                    }
                    val pages = document.select("p.pagination span.items").first().text()
                    val totalCount = pages.substring(pages.lastIndexOf("/") + 1, pages.lastIndexOf(")"))
                    result = AddressBook(userList, Integer.parseInt(totalCount), page)
                } catch (e: IOException) {
                    e.printStackTrace()
                    result = AddressBook(null, 0, page)
                }

                return result
            }

            private fun parseUser(elements: Elements, index: Int): User {
                val td = elements[index].select("td")

                val username = td.first().select("a").text()
                val email = td[1].select("a").text()
                val phone = td[2].text()
                val qq = td[3].text()
                val wechat = td[4].text()
                var pinyin = chinese2Pinyin(username.trim())

                return User(username.trim(), email, phone, qq, wechat, pinyin)
            }

            override fun onPostExecute(addressBook: AddressBook) {
                super.onPostExecute(addressBook)
                listener(addressBook)
            }
        }, pageIndex)

    }

    fun login(name: String, pwd: String, listener: (Boolean) -> Unit): AsyncTask<*, *, *> {
        return AsyncTaskCompat.executeParallel(object : AsyncTask<String, Void, Boolean>() {
            override fun doInBackground(vararg params: String): Boolean? {
                var login: Boolean
                try {
                    //准备登陆数据
                    var call = api.loginPageHtml()
                    var response = call.execute()
                    val document = Jsoup.parse(response.body(), "UTF-8")
                    val content = document.select("meta[name=csrf-token]").first().attr("content")

                    // 登陆账号
                    call = api.login("✓", content, params[0], params[1], "Login »")
                    response = call.execute()

                    login = response.code() == 200 && !TextUtils.isEmpty(response.headers().get("Set-Cookie"))
                } catch (e: IOException) {
                    e.printStackTrace()
                    login = false
                }

                return login
            }

            override fun onPostExecute(aBoolean: Boolean) {
                super.onPostExecute(aBoolean)
                listener(aBoolean)
            }
        }, name, pwd)
    }
}

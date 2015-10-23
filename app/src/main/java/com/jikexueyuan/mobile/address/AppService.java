package com.jikexueyuan.mobile.address;

import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.text.TextUtils;
import android.util.Log;

import com.jikexueyuan.mobile.address.bean.AddressBook;
import com.jikexueyuan.mobile.address.bean.User;
import com.squareup.okhttp.OkHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by aven on 10/22/15.
 */
public class AppService {
    static API api;

    static {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(client.getConnectTimeout() * 2, TimeUnit.MILLISECONDS);
        client.setReadTimeout(client.getReadTimeout() * 2, TimeUnit.MILLISECONDS);
        api = new Retrofit.Builder().baseUrl("http://work.eoemobile.com")
                .addConverterFactory(new StringConvertFactory())
                .client(client)
                .build().create(API.class);
    }

    public static AsyncTask<?, ?, ?> getAddressBookByPage(int pageIndex, final Block<AddressBook> listener) {
        return AsyncTaskCompat.executeParallel(new AsyncTask<Integer, Void, AddressBook>() {
            @Override
            protected AddressBook doInBackground(Integer... params) {
                final int page = params[0];
                try {
                    Call<String> call = api.addressBooks(page, 20);
                    Response<String> response = call.execute();
                    //User Info
                    Document document = Jsoup.parse(response.body(), "UTF-8");
                    Elements userTable = document.select("div table tbody tr");

                    List<User> userList = new ArrayList<>();
                    for (int index = 0; index < userTable.size(); index++) {
                        userList.add(parseUser(userTable, index));
                    }
                    String pages = document.select("p.pagination span.items").first().text();
                    String totalCount = pages.substring(pages.lastIndexOf("/") + 1, pages.lastIndexOf(")"));
                    return new AddressBook(userList, Integer.parseInt(totalCount), page);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new AddressBook(null, 0, page);
            }

            private User parseUser(Elements elements, int index) {
                Elements td = elements.get(index).select("td");

                String username = td.first().select("a").text();
                String email = td.get(1).select("a").text();
                String phone = td.get(2).text();
                String qq = td.get(3).text();
                String wechat = td.get(4).text();
                return new User(username, email, phone, qq, wechat);
            }

            @Override
            protected void onPostExecute(AddressBook addressBook) {
                super.onPostExecute(addressBook);
                listener.onCallback(addressBook);
            }
        }, pageIndex);

    }

    public static AsyncTask<?, ?, ?> login(String name, String pwd, final Block<Boolean> listener) {
        return AsyncTaskCompat.executeParallel(new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                boolean login = false;
                try {
                    //准备登陆数据
                    Call<String> call = api.getLoginPageHtml();
                    Response<String> response = call.execute();
                    Document document = Jsoup.parse(response.body(), "UTF-8");
                    String content = document.select("meta[name=csrf-token]").first().attr("content");

                    // 登陆账号
                    call = api.login("✓", content, params[0], params[1], "Login »");
                    response = call.execute();

                    login = response.code() == 200 && !TextUtils.isEmpty(response.headers().get("Set-Cookie"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return login;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                listener.onCallback(aBoolean);
            }
        }, name, pwd);
    }

    public interface Block<T> {
        void onCallback(T data);
    }
}

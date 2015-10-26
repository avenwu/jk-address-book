package com.jikexueyuan.mobile.address;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.text.TextUtils;
import android.util.Log;

import com.jikexueyuan.mobile.address.api.StringConvertFactory;
import com.jikexueyuan.mobile.address.bean.User;
import com.squareup.okhttp.OkHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * [Testing Fundamentals](http://d.android.com/tools/testing/testing_android.html)
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    Retrofit retrofit;
    ApiService api;
    //TODO remove
    String username = BuildConfig.USER_NAME;
    String password = BuildConfig.PASSWORD;

    public ApplicationTest() {
        super(Application.class);
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(client.getConnectTimeout() * 2, TimeUnit.MILLISECONDS);
        retrofit = new Retrofit.Builder().baseUrl("http://work.eoemobile.com")
                .addConverterFactory(new StringConvertFactory())
                .client(client)
                .build();
        api = retrofit.create(ApiService.class);
    }

    public void testLoginHtmlMetaToken() throws IOException {
        Call<String> call = api.getLoginPageHtml();
        Response<String> response = call.execute();
        assertNotNull(response);
        Document document = Jsoup.parse(response.body(), "UTF-8");
        String content = document.select("meta[name=csrf-token]").first().attr("content");
        assertNotNull(content);
    }

    public void testLoginAndAddressBook() throws IOException {
        //准备登陆数据
        Call<String> call = api.getLoginPageHtml();
        Response<String> response = call.execute();
        assertNotNull(response);
        Document document = Jsoup.parse(response.body(), "UTF-8");
        String content = document.select("meta[name=csrf-token]").first().attr("content");
        assertNotNull(content);

        // 登陆账号
        call = api.login("✓", content, username, password, "Login »");
        response = call.execute();
        assertNotNull(response);
        assertEquals(200, response.code());
        assertFalse(TextUtils.isEmpty(response.headers().get("Set-Cookie")));
        Log.e("HTML", response.body());

        call = api.addressBooks(1, 20);
        response = call.execute();
        assertNotNull(response.body());

        //User Info
        document = Jsoup.parse(response.body(), "UTF-8");
        assertNotNull(document);
        Elements userTable = document.select("div table tbody tr");
        assertNotNull(userTable);

        List<User> userList = new ArrayList<>();
        for (int index = 0; index < userTable.size(); index++) {
            userList.add(parseUser(userTable, index));
        }
        assertTrue(userList.size() > 0);

        String pages = document.select("p.pagination span.items").first().text();
        assertEquals(pages, "(1-25/108)");
        String totalCount = pages.substring(pages.lastIndexOf("/") + 1, pages.lastIndexOf(")"));
        assertEquals(totalCount, "108");
    }

    interface ApiService {

        @GET("login")
        Call<String> getLoginPageHtml();

        @FormUrlEncoded
        @POST("login")
        Call<String> login(@Field("utf8") String uft8,
                           @Field("authenticity_token") String authenticity_token,
                           @Field("username") String username,
                           @Field("password") String password,
                           @Field("login") String login);

        @GET("address_books")
        Call<String> addressBooks(@Query("page") int page, @Query("per_page") int per_page);
    }

    public void testAddressHtmlParse() throws IOException {
        InputStream inputStream = getContext().getAssets().open("address.html");
        Document document = Jsoup.parse(inputStream, "UTF-8", "");
        inputStream.close();
        assertNotNull(document);
        Elements userTable = document.select("div table tbody tr");
        assertNotNull(userTable);

        List<User> userList = new ArrayList<>();
        for (int index = 0; index < userTable.size(); index++) {
            userList.add(parseUser(userTable, index));
        }
        assertTrue(userList.size() > 0);

        String pages = document.select("p.pagination span.items").first().text();
        assertEquals(pages, "(1-25/108)");
        String totalCount = pages.substring(pages.lastIndexOf("/") + 1, pages.lastIndexOf(")"));
        assertEquals(totalCount, "108");
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
}
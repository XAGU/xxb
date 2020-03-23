package com.xagu.xxb.utils;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.xagu.xxb.base.BaseApplication;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by XAGU on 2020/3/14
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class RetrofitManager {
    private static final RetrofitManager ourInstance = new RetrofitManager();
    private final Retrofit mRetrofit;

    public static RetrofitManager getInstance() {
        return ourInstance;
    }

    private RetrofitManager() {
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(BaseApplication.getAppContext()));
        //设置一下okHttp的参数
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        //创建Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://i.chaoxing.com")
                .build();
    }

    public Retrofit getRetrofit(){
        return mRetrofit;
    }

}

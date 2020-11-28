package com.xagu.xxb.Interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author xagu
 * Created on 2020/9/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class UserAgentInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        //    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
        Request request = chain.request()
                .newBuilder()
                .addHeader("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
                .build();
        return chain.proceed(request);
    }
}

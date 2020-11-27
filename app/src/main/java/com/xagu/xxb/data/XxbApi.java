package com.xagu.xxb.data;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by XAGU on 2020/3/14
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface XxbApi {
    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @POST("https://passport2.chaoxing.com/fanyalogin?t=true&fid=-1&refer=http%3A%2F%2Fi.chaoxing.com")
    Call<ResponseBody> login(@Query("uname") String username, @Query("password") String password);
    
    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("https://sso.chaoxing.com:443/apis/login/userLogin4Uname.do")
    Call<ResponseBody> getUserInfo();

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("http://mooc1-api.chaoxing.com/mycourse/backclazzdata?view=json&rss=1")
    Call<ResponseBody> getCourseInfo();

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("https://mobilelearn.chaoxing.com/ppt/activeAPI/taskactivelist")
    Call<ResponseBody> getActiveInfo(@Query("courseId") String courseId, @Query("classId") String classId, @Query("uid") String uid);

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET()
    Call<ResponseBody> checkSignStatus(@Url String url);

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("https://mobilelearn.chaoxing.com/widget/sign/pcTeaSignController/endSign")
    Call<ResponseBody> checkSignType(@Query("activeId") String activeId, @Query("classId") String classId, @Query("courseId") String courseId, @Query("isTeacherViewOpen") String isTeacherViewOpen);

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("https://mobilelearn.chaoxing.com:443/v2/apis/sign/refreshQRCode")
    Call<ResponseBody> getQrCode(@Query("activeId") String activeId, @Query("time") String time);

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("https://mobilelearn.chaoxing.com/pptSign/stuSignajax?&clientip=8.8.8.8&&appType=15&ifTiJiao=true")
    Call<ResponseBody> sign(@Query("address") String address, @Query("name") String name, @Query("longitude") String lng, @Query("latitude") String lat, @Query("uid") String uid, @Query("activeId") String activeId, @Query("objectId") String objectId);

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("https://pan-yz.chaoxing.com:443/api/token/uservalid")
    Call<ResponseBody> getPanToken();

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @Multipart
    @POST("https://pan-yz.chaoxing.com:443/upload")
    Call<ResponseBody> uploadImg(@Query("_token") String _token, @Part MultipartBody.Part file, @Query("puid") String puid);

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("https://passport2-api.chaoxing.com:443/api/sendcaptcha")
    Call<ResponseBody> requestPhoneCode(@Query("to") String phone, @Query("countrycode") String countryCode, @Query("time") String time, @Query("enc") String enc);

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("https://passport2-api.chaoxing.com:443/v11/loginregister?cx_xxt_passport=json&countrycode=86&loginType=2&roleSelect=true")
    Call<ResponseBody> loginByCode(@Query("uname") String username, @Query("code") String phoneCode);

    @Headers("User-Agent:Dalvik/2.1.0 (Linux; U; Android 5.1.1; vmos Build/LMY48G) com.chaoxing.mobile/ChaoXingStudy_3_4.7.3_android_phone_592_53 (@Kalimdor)_e3e83ea1cea14f448ecf40ed64c08653")
    @GET("https://mobilelearn.chaoxing.com:443/pptAnswer/stuAnswer?role=&general=1&appType=15&stuMiddlePage=1")
    Call<ResponseBody> AnswerRace(@Query("answerId") String answerId, @Query("classId") String classId, @Query("courseId") String courseId, @Query("stuName") String stuName);
}

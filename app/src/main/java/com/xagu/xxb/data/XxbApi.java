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

    @POST("https://passport2.chaoxing.com/fanyalogin?t=true&fid=-1&refer=http%3A%2F%2Fi.chaoxing.com")
    Call<ResponseBody> login(@Query("uname") String username, @Query("password") String password);

    @GET("https://sso.chaoxing.com:443/apis/login/userLogin4Uname.do")
    Call<ResponseBody> getUserInfo();

    @GET("http://mooc1-api.chaoxing.com/mycourse/backclazzdata?view=json&rss=1")
    Call<ResponseBody> getCourseInfo();

    @GET("https://mobilelearn.chaoxing.com/ppt/activeAPI/taskactivelist")
    Call<ResponseBody> getActiveInfo(@Query("courseId") String courseId, @Query("classId") String classId, @Query("uid") String uid);

    @GET()
    Call<ResponseBody> checkSignStatus(@Url String url);

    @GET("https://mobilelearn.chaoxing.com/widget/sign/pcTeaSignController/endSign")
    Call<ResponseBody> checkSignType(@Query("activeId") String activeId, @Query("classId") String classId, @Query("courseId") String courseId, @Query("isTeacherViewOpen") String isTeacherViewOpen);

    @GET("https://mobilelearn.chaoxing.com:443/v2/apis/sign/refreshQRCode")
    Call<ResponseBody> getQrCode(@Query("activeId") String activeId, @Query("time") String time);

    @GET("https://mobilelearn.chaoxing.com/pptSign/stuSignajax?&clientip=8.8.8.8&&appType=15&ifTiJiao=true")
    Call<ResponseBody> sign(@Query("address") String address, @Query("name") String name, @Query("longitude") String lng, @Query("latitude") String lat, @Query("uid") String uid, @Query("activeId") String activeId, @Query("objectId") String objectId);

    @GET("https://pan-yz.chaoxing.com:443/api/token/uservalid")
    Call<ResponseBody> getPanToken();

    @Multipart
    @POST("https://pan-yz.chaoxing.com:443/upload")
    Call<ResponseBody> uploadImg(@Query("_token") String _token, @Part MultipartBody.Part file, @Query("puid") String puid);

    @GET("https://passport2-api.chaoxing.com:443/api/sendcaptcha")
    Call<ResponseBody> requestPhoneCode(@Query("to") String phone, @Query("countrycode") String countryCode, @Query("time") String time, @Query("enc") String enc);

    @GET("https://passport2-api.chaoxing.com:443/v11/loginregister?cx_xxt_passport=json&countrycode=86&loginType=2&roleSelect=true")
    Call<ResponseBody> loginByCode(@Query("uname") String username, @Query("code") String phoneCode);

    @GET("https://mobilelearn.chaoxing.com:443/pptAnswer/stuAnswer?role=&general=1&appType=15&stuMiddlePage=1")
    Call<ResponseBody> AnswerRace(@Query("answerId") String answerId, @Query("classId") String classId, @Query("courseId") String courseId, @Query("stuName") String stuName);
}

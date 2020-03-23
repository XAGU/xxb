package com.xagu.xxb.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umeng.analytics.MobclickAgent;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.bean.Course;
import com.xagu.xxb.bean.User;
import com.xagu.xxb.data.XxbApi;
import com.xagu.xxb.interfaces.IAccountCallback;
import com.xagu.xxb.interfaces.IAccountPresenter;
import com.xagu.xxb.interfaces.ICourseCallback;
import com.xagu.xxb.interfaces.ICoursePresenter;
import com.xagu.xxb.utils.RetrofitManager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class CoursePresenter implements ICoursePresenter {

    private volatile static CoursePresenter sInstance = null;
    List<ICourseCallback> mCallbacks = new ArrayList<>();
    private final XxbApi mXxbApi;


    private CoursePresenter() {
        Retrofit retrofit = RetrofitManager.getInstance().getRetrofit();
        mXxbApi = retrofit.create(XxbApi.class);
    }

    public static CoursePresenter getInstance() {
        if (sInstance == null) {
            synchronized (CoursePresenter.class) {
                if (sInstance == null) {
                    sInstance = new CoursePresenter();
                }
            }
        }
        return sInstance;
    }


    @Override
    public void getCourseInfo() {
        Call<ResponseBody> task = mXxbApi.getCourseInfo();
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                try {
                    if (body != null) {
                        String s = body.string();
                        List<Course> courses = analysisCourseInfo(s);
                        for (ICourseCallback callback : mCallbacks) {
                            callback.onGetCourseSuccess(courses);
                        }
                    } else {
                        for (ICourseCallback callback : mCallbacks) {
                            callback.onGetCourseFailed();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    for (ICourseCallback callback : mCallbacks) {
                        callback.onGetCourseFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
                for (ICourseCallback callback : mCallbacks) {
                    callback.onGetCourseNetworkError();
                }
            }
        });
    }

    /**
     * 解析查询Course返回的json
     *
     * @param response
     */
    private List<Course> analysisCourseInfo(String response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Course> courses = new ArrayList<>();
        JsonNode readTree = objectMapper.readTree(response);
        if (readTree.get("result").asInt() == 0) {
            return null;
        }
        JsonNode channelList = readTree.get("channelList");
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).get("cataid").asInt() == 100000002) {
                JsonNode content = channelList.get(i).get("content");
                if (!content.has("course")) {
                    continue;
                }
                Course course = new Course();
                if (content.has("id")) {
                    course.setClassId(content.get("id").asText());
                }else {
                    course.setClassId("");
                }
                if (content.has("name")) {
                    course.setClassname(content.get("name").asText());
                } else {
                    course.setClassname("");
                }
                JsonNode courseData = content.findValue("data").get(0);
                if (courseData.has("id")) {
                    course.setCourseId(courseData.get("id").asText());
                } else {
                    course.setCourseId("");
                }
                if (courseData.has("teacherfactor")) {
                    course.setTeacher(courseData.get("teacherfactor").asText());
                } else {
                    course.setTeacher("");
                }
                if (courseData.has("imageurl")) {
                    course.setImageUrl(courseData.get("imageurl").asText());
                } else {
                    course.setImageUrl("");
                }
                if (courseData.has("name")) {
                    course.setName(courseData.get("name").asText());
                } else {
                    course.setName("");
                }
                if (courseData.has("courseSquareUrl")) {
                    String courseSquareUrl = courseData.get("courseSquareUrl").asText();
                    course.setUid(courseSquareUrl.substring(courseSquareUrl.lastIndexOf("/") + 1));
                } else {
                    course.setUid("");
                }
                courses.add(course);
            }
        }
        return courses;
    }

    @Override
    public void registerViewCallback(ICourseCallback iCourseCallback) {
        if (!mCallbacks.contains(iCourseCallback)) {
            mCallbacks.add(iCourseCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ICourseCallback iCourseCallback) {
        mCallbacks.remove(iCourseCallback);
    }
}

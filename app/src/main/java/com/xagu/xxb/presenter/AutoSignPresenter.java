package com.xagu.xxb.presenter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umeng.analytics.MobclickAgent;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.bean.Active;
import com.xagu.xxb.bean.Course;
import com.xagu.xxb.data.XxbApi;
import com.xagu.xxb.interfaces.IAutoSignCallback;
import com.xagu.xxb.interfaces.IAutoSignPresenter;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.RetrofitManager;
import com.xagu.xxb.utils.SPUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class AutoSignPresenter implements IAutoSignPresenter {

    private volatile static AutoSignPresenter sInstance = null;
    List<IAutoSignCallback> mCallbacks = new ArrayList<>();
    private final XxbApi mXxbApi;
    private List<Course> mCourses;
    public boolean isRun = false;
    int count = 0;
    int signSuccess = 0;


    private AutoSignPresenter() {
        Retrofit retrofit = RetrofitManager.getInstance().getRetrofit();
        mXxbApi = retrofit.create(XxbApi.class);
    }

    public static AutoSignPresenter getInstance() {
        if (sInstance == null) {
            synchronized (AutoSignPresenter.class) {
                if (sInstance == null) {
                    sInstance = new AutoSignPresenter();
                }
            }
        }
        return sInstance;
    }


    @Override
    public void registerViewCallback(IAutoSignCallback iAutoSignCallback) {
        if (!mCallbacks.contains(iAutoSignCallback)) {
            mCallbacks.add(iAutoSignCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(IAutoSignCallback iAutoSignCallback) {
        mCallbacks.remove(iAutoSignCallback);
    }

    public void deleteSubSign(Course course) {
        if (mCourses.contains(course)) {
            mCourses.remove(course);

            for (IAutoSignCallback callback : mCallbacks) {
                if (mCourses.size() != 0) {
                    callback.onGetAutoSignCourseSuccess(mCourses);
                } else {
                    callback.onGetAutoSignCourseEmpty();
                }
            }

        }
    }

    public void addSubSign(Course course) {
        if (!mCourses.contains(course)) {
            mCourses.add(course);
            for (IAutoSignCallback callback : mCallbacks) {
                callback.onGetAutoSignCourseSuccess(mCourses);
            }
        }
    }

    @Override
    public void getAutoSignCourse() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, ?> courseMap = SPUtil.getAll(Constants.SP_SUB_SIGN);
            mCourses = new ArrayList<>();
            for (Map.Entry<String, ?> entry : courseMap.entrySet()) {
                Course course = objectMapper.readValue((String) entry.getValue(), Course.class);
                mCourses.add(course);
            }
            for (IAutoSignCallback callback : mCallbacks) {
                if (mCourses.size() == 0) {
                    callback.onGetAutoSignCourseEmpty();
                } else {
                    callback.onGetAutoSignCourseSuccess(mCourses);
                }
            }
        } catch (JsonProcessingException e) {
            for (IAutoSignCallback callback : mCallbacks) {
                callback.onGetAutoSignCourseEmpty();
            }
            e.printStackTrace();
            MobclickAgent.reportError(BaseApplication.getAppContext(), e);
        }
    }

    public void stopSign() {
        isRun = false;
    }

    @Override
    public void autoSign(List<Course> course) {
        isRun = true;
        //取参数
        String address = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_ADDRESS, "", Constants.SP_CONFIG);
        String lng = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_LNG, "", Constants.SP_CONFIG);
        String lat = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_LAT, "", Constants.SP_CONFIG);
        String name = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_NAME, "", Constants.SP_CONFIG);
        String photo = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_PHOTO, "", Constants.SP_CONFIG);
        String delay = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_DELAY, "0", Constants.SP_CONFIG);
        //获取活动
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRun) {
                    for (Course course1 : course) {
                        List<Active> actives = getActiveInfo(course1);
                        if (actives == null) {
                            continue;
                        }
                        for (Active active : actives) {
                            if (requestActiveSignStatus(active)) {
                                //没签到的
                                if (sign(address, name, lng, lat, course1.getUid(), active.getId(), photo)) {
                                    //成功签到，通知ui
                                    for (IAutoSignCallback callback : mCallbacks) {
                                        BaseApplication.getsHandler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.signLog(count, ++signSuccess);
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                    for (IAutoSignCallback callback : mCallbacks) {
                        BaseApplication.getsHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                callback.signLog(++count,signSuccess);
                            }
                        });
                    }
                    try {
                        Thread.sleep(Integer.parseInt(delay));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public boolean sign(String address, String name, String lng, String lat, String uid, String activeId, String objectId) {
        Call<ResponseBody> task = mXxbApi.sign(address, name, lng, lat, uid, activeId, objectId);
        try {
            Response<ResponseBody> response = task.execute();
            ResponseBody body = response.body();
            if (body != null) {
                //成功
                return true;
            } else {
                return false;
                //失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            MobclickAgent.reportError(BaseApplication.getAppContext(), e);
            return false;
        }
    }


    private List<Active> getActiveInfo(Course course) {
        String courseId = course.getCourseId();
        String classId = course.getClassId();
        String uid = course.getUid();
        Call<ResponseBody> task = mXxbApi.getActiveInfo(courseId, classId, uid);
        try {
            Response<ResponseBody> response = task.execute();
            ResponseBody body = response.body();
            if (body != null) {
                String s = body.string();
                //筛选已经签到的
                return analysisSignActiveInfo(s);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MobclickAgent.reportError(BaseApplication.getAppContext(), e);
            return null;
        }
    }


    /**
     * 解析查询Course返回的json
     *
     * @param response
     */
    private List<Active> analysisSignActiveInfo(String response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Active> actives = new ArrayList<>();
        JsonNode readTree = objectMapper.readTree(response);
        JsonNode activeList = readTree.get("activeList");
        for (JsonNode jsonNode : activeList) {
            String status;
            if (jsonNode.has("status")) {
                status = jsonNode.get("status").asText();
            } else {
                continue;
            }
            String activeType;
            if (jsonNode.has("activeType")) {
                activeType = jsonNode.get("activeType").asText();
            } else {
                continue;
            }
            if (activeType.equals("19")) {
                continue;
            }
            if (!activeType.equals("2") || !status.equals("1")) {
                break;
            }
            Active active = new Active();
            active.setStatus(status);
            active.setActiveType(activeType);
            if (jsonNode.has("id")) {
                active.setId(jsonNode.get("id").asText());
            } else {
                active.setId("");
            }
            if (jsonNode.has("nameOne")) {
                active.setName(jsonNode.get("nameOne").asText());
            } else {
                active.setName("");
            }
            if (jsonNode.has("nameFour")) {
                active.setTime(jsonNode.get("nameFour").asText());
            } else {
                active.setTime("");
            }
            if (jsonNode.has("url")) {
                active.setUrl(jsonNode.get("url").asText());
            } else {
                active.setUrl("");
            }
            if (jsonNode.has("picUrl")) {
                active.setCover_url(jsonNode.get("picUrl").asText());
            } else {
                active.setCover_url("");
            }
            actives.add(active);
        }
        return actives;
    }

    private boolean requestActiveSignStatus(Active active) {
        Call<ResponseBody> task = mXxbApi.checkSignStatus(active.getUrl());
        try {
            Response<ResponseBody> response = task.execute();
            Document doc = Jsoup.parse(response.body().string());
            if (Constants.SIGN_STATUS_SUCCESS.equals(doc.select("#statuscontent").text())) {
                //签到了
                return false;
            } else {
                //没签到
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            MobclickAgent.reportError(BaseApplication.getAppContext(), e);
            return false;
        }
    }
}

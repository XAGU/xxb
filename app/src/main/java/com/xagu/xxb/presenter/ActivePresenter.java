package com.xagu.xxb.presenter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umeng.analytics.MobclickAgent;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.bean.Active;
import com.xagu.xxb.bean.Course;
import com.xagu.xxb.data.XxbApi;
import com.xagu.xxb.interfaces.IActiveCallback;
import com.xagu.xxb.interfaces.IActivePresenter;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.RetrofitManager;
import com.xagu.xxb.utils.SPUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
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
public class ActivePresenter implements IActivePresenter {

    private volatile static ActivePresenter sInstance = null;
    List<IActiveCallback> mCallbacks = new ArrayList<>();
    private final XxbApi mXxbApi;
    private Course mTargetCourse = null;
    private boolean isSub = false;


    private ActivePresenter() {
        Retrofit retrofit = RetrofitManager.getInstance().getRetrofit();
        mXxbApi = retrofit.create(XxbApi.class);
    }

    public static ActivePresenter getInstance() {
        if (sInstance == null) {
            synchronized (ActivePresenter.class) {
                if (sInstance == null) {
                    sInstance = new ActivePresenter();
                }
            }
        }
        return sInstance;
    }

    public void setTargetCourse(Course course) {
        this.mTargetCourse = course;
        if (SPUtil.contains(mTargetCourse.getCourseId(),Constants.SP_SUB_SIGN)) {
            this.isSub = true;
        } else {
            isSub = false;
        }
    }

    public boolean isSub() {
        return isSub;
    }

    @Override
    public void requestActiveSignStatus(Active active) {
        Call<ResponseBody> task = mXxbApi.checkSignStatus(active.getUrl());
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Document doc = null;
                try {
                    doc = Jsoup.parse(response.body().string());
                    if (Constants.SIGN_STATUS_SUCCESS.equals(doc.select("#statuscontent").text())) {
                        //签到了
                        active.setSigned(true);
                    } else {
                        active.setSigned(false);
                    }
                    for (IActiveCallback callback : mCallbacks) {
                        callback.onRequestActiveDetailSuccess(active);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    for (IActiveCallback callback : mCallbacks) {
                        callback.onRequestActiveDetailFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                for (IActiveCallback callback : mCallbacks) {
                    callback.onRequestActiveDetailNetworkError();
                }
            }
        });
    }


    @Override
    public void requestActiveType(Active active) {
        //以教师视角打开
        Call<ResponseBody> task = mXxbApi.checkSignType(active.getUrl().replace("isTeacherViewOpen=0", "isTeacherViewOpen=1"));
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Document doc = null;
                try {
                    doc = Jsoup.parse(response.body().string());
                    switch (Integer.parseInt(doc.select("#otherId").val())) {
                        case 3:
                            active.setActiveTypeName(Constants.SIGN_TYPE_GESTURE);
                            //记录下手势码
                            active.setSignCode(doc.select("#signCode").val());
                            break;
                        case 0:
                            //普通签到或者拍照签到
                            if (Integer.parseInt(doc.select("#ifPhoto").val()) == 1) {
                                //拍照签到
                                active.setActiveTypeName(Constants.TYPE_SIGN_PHOTO);
                            } else {
                                //普通签到
                                active.setActiveTypeName(Constants.TYPE_SIGN_COMMON);
                            }
                            break;
                        case 4:
                            //位置签到
                            active.setActiveTypeName(Constants.TYPE_SIGN_LOCATION);
                            break;
                        case 2:
                            //二维码签到
                            if (Integer.parseInt(doc.select("#ifRefreshEwm").val()) == 1) {
                                //随机二维码
                                active.setActiveTypeName(Constants.TYPE_SIGN_RANDOM_QR);
                            } else {
                                //普通二维码
                                active.setActiveTypeName(Constants.TYPE_SIGN_QR);
                            }
                            break;
                        default:
                            //不知道
                            active.setActiveTypeName(Constants.TYPE_SIGN_UNKNOWN);
                            break;
                    }
                    //suucess
                    requestActiveSignStatus(active);
                } catch (IOException e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    for (IActiveCallback callback : mCallbacks) {
                        callback.onRequestActiveDetailFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                for (IActiveCallback callback : mCallbacks) {
                    callback.onRequestActiveDetailNetworkError();
                }
            }
        });
    }

    @Override
    public void sub() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SPUtil.put(mTargetCourse.getCourseId(), objectMapper.writeValueAsString(mTargetCourse), Constants.SP_SUB_SIGN);
            isSub = true;
            AutoSignPresenter.getInstance().addSubSign(mTargetCourse);
            //TODO,通知监控的presenter，数据更新
        } catch (JsonProcessingException e) {
            MobclickAgent.reportError(BaseApplication.getAppContext(), e);
            e.printStackTrace();
        }
    }

    @Override
    public void unSub() {
        //TODO,通知监控的presenter，数据更新
        SPUtil.remove(mTargetCourse.getCourseId(), Constants.SP_SUB_SIGN);
        isSub = false;
        AutoSignPresenter.getInstance().deleteSubSign(mTargetCourse);
    }

    @Override
    public Course getTargetCourse() {
        return mTargetCourse;
    }


    @Override
    public void getActiveInfo() {
        String courseId = mTargetCourse.getCourseId();
        String classId = mTargetCourse.getClassId();
        String uid = mTargetCourse.getUid();
        Call<ResponseBody> task = mXxbApi.getActiveInfo(courseId, classId, uid);
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                try {
                    if (body != null) {
                        String s = body.string();
                        List<Active> actives = analysisActiveInfo(s);
                        for (IActiveCallback callback : mCallbacks) {
                            callback.onGetActiveSuccess(actives);
                        }
                    } else {
                        for (IActiveCallback callback : mCallbacks) {
                            callback.onGetActiveFailed();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    for (IActiveCallback callback : mCallbacks) {
                        callback.onGetActiveFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
                for (IActiveCallback callback : mCallbacks) {
                    callback.onGetActiveNetworkError();
                }
            }
        });
    }


    /**
     * 解析查询Course返回的json
     *
     * @param response
     */
    private List<Active> analysisActiveInfo(String response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Active> actives = new ArrayList<>();
        JsonNode readTree = objectMapper.readTree(response);
        JsonNode activeList = readTree.get("activeList");
        for (JsonNode jsonNode : activeList) {
            Active active = new Active();
            if (jsonNode.has("id")) {
                active.setId(jsonNode.get("id").asText());
            } else {
                active.setId("");
            }
            if (jsonNode.has("activeType")) {
                active.setActiveType(jsonNode.get("activeType").asText());
            } else {
                active.setActiveType("");
            }
            if (jsonNode.has("nameOne")) {
                active.setName(jsonNode.get("nameOne").asText());
            } else {
                active.setName("");
            }
            if (jsonNode.has("status")) {
                active.setStatus(jsonNode.get("status").asText());
            } else {
                active.setStatus("");
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


    @Override
    public void registerViewCallback(IActiveCallback iActiveCallback) {
        if (!mCallbacks.contains(iActiveCallback)) {
            mCallbacks.add(iActiveCallback);
            if (mTargetCourse != null) {
                iActiveCallback.onCourseLoaded(mTargetCourse);
            }
        }
    }

    @Override
    public void unRegisterViewCallback(IActiveCallback iActiveCallback) {
        mCallbacks.remove(iActiveCallback);
    }
}

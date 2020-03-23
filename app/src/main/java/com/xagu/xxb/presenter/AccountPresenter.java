package com.xagu.xxb.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umeng.analytics.MobclickAgent;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.bean.User;
import com.xagu.xxb.data.XxbApi;
import com.xagu.xxb.interfaces.IAccountCallback;
import com.xagu.xxb.interfaces.IAccountPresenter;
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
public class AccountPresenter implements IAccountPresenter {

    private volatile static AccountPresenter sInstance = null;
    List<IAccountCallback> mCallbacks = new ArrayList<>();
    private final XxbApi mXxbApi;


    private AccountPresenter() {
        Retrofit retrofit = RetrofitManager.getInstance().getRetrofit();
        mXxbApi = retrofit.create(XxbApi.class);
    }

    public static AccountPresenter getInstance() {
        if (sInstance == null) {
            synchronized (AccountPresenter.class) {
                if (sInstance == null) {
                    sInstance = new AccountPresenter();
                }
            }
        }
        return sInstance;
    }


    @Override
    public void getUserInfo() {
        Call<ResponseBody> task = mXxbApi.getUserInfo();
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                try {
                    if (body != null) {
                        String s = body.string();
                        User user = analysisUserInfo(s);
                        for (IAccountCallback callback : mCallbacks) {
                            callback.onGetUserInfoSuccess(user);
                        }
                    } else {
                        for (IAccountCallback callback : mCallbacks) {
                            callback.onGetUserInfoFailed();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    for (IAccountCallback callback : mCallbacks) {
                        callback.onGetUserInfoFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
                for (IAccountCallback callback : mCallbacks) {
                    callback.onGetUserInfoNetworkError();
                }
            }
        });
    }

    /**
     * 解析查询userinfo返回的json
     * @param response
     */
    private User analysisUserInfo(String response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode readTree = objectMapper.readTree(response);
        if (readTree.findValue("result").asInt()==0) {
            return null;
        }
        User user = new User();
        JsonNode cx_fanya = readTree.findValue("cx_fanya");
        if (cx_fanya == null) {
            return null;
        }
        if (cx_fanya.has("schoolname")) {
            user.setSchoolName(cx_fanya.findValue("schoolname").asText());
        } else {
            user.setSchoolName("");
        }
        if (cx_fanya.has("realname")) {
            user.setNickname(cx_fanya.findValue("realname").asText());
        } else {
            user.setNickname("");
        }
        if (cx_fanya.has("uid")) {
            user.setUid(cx_fanya.findValue("uid").asText());
        } else {
            user.setUid("");
        }
        if (cx_fanya.has("uname")) {
            user.setStudentId(cx_fanya.findValue("uname").asText());
        } else {
            user.setStudentId("");
        }
        if (cx_fanya.has("phone")) {
            user.setPhoneNum(cx_fanya.findValue("phone").asText());
        } else {
            user.setPhoneNum("");
        }
        if (cx_fanya.has("email")) {
            user.setEmail(cx_fanya.findValue("email").asText());
        } else {
            user.setEmail("");
        }
        if (cx_fanya.has("msg")) {
            JsonNode msg = readTree.get("msg");
            if (msg.has("pic")){
                user.setHeadPic(msg.get("pic").asText());
            } else {
                user.setHeadPic("");
            }
        } else {
            user.setHeadPic("");
        }
        return user;
    }

    @Override
    public void registerViewCallback(IAccountCallback iLoginCallback) {
        if (!mCallbacks.contains(iLoginCallback)) {
            mCallbacks.add(iLoginCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(IAccountCallback iLoginCallback) {
        mCallbacks.remove(iLoginCallback);
    }
}

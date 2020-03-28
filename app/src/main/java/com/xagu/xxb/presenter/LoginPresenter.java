package com.xagu.xxb.presenter;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umeng.analytics.MobclickAgent;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.data.XxbApi;
import com.xagu.xxb.interfaces.ILoginCallback;
import com.xagu.xxb.interfaces.ILoginPresenter;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.MD5Util;
import com.xagu.xxb.utils.RetrofitManager;
import com.xagu.xxb.utils.SPUtil;

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
public class LoginPresenter implements ILoginPresenter {

    private volatile static LoginPresenter sInstance = null;
    List<ILoginCallback> mCallbacks = new ArrayList<>();
    private final XxbApi mXxbApi;


    private LoginPresenter() {
        Retrofit retrofit = RetrofitManager.getInstance().getRetrofit();
        mXxbApi = retrofit.create(XxbApi.class);
    }

    public static LoginPresenter getInstance() {
        if (sInstance == null) {
            synchronized (LoginPresenter.class) {
                if (sInstance == null) {
                    sInstance = new LoginPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void Login(final String username, final String password) {
        Call<ResponseBody> task = mXxbApi.login(username, password);
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //数据
                try {
                    ResponseBody body = response.body();
                    for (ILoginCallback callback : mCallbacks) {
                        if (body != null && body.string().contains("{\"success\":true}")) {
                            //登录成功
                            //记录账号
                            SPUtil.put(Constants.SP_CONFIG_LOGIN_TYPE, Constants.LOGIN_TYPE_PASSWORD, Constants.SP_CONFIG);
                            SPUtil.put("username", username, Constants.SP_CONFIG);
                            SPUtil.put("password", password, Constants.SP_CONFIG);
                            //通知UI
                            callback.onLoginSuccess();
                        } else {
                            //登录失败
                            callback.onLoginFailed();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    for (ILoginCallback callback : mCallbacks) {
                        callback.onLoginFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
                for (ILoginCallback callback : mCallbacks) {
                    callback.onNetworkError();
                }
            }
        });
    }

    @Override
    public boolean isLogin() {
        String username = (String) SPUtil.get("username", null, Constants.SP_CONFIG);
        String password = (String) SPUtil.get("password", null, Constants.SP_CONFIG);
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            //有账号存在，登录，但是我们还是得刷新下登录状态
            Login(username, password);
            return true;
        } else {
            //没有账号存在，未登录
            return false;
        }
    }

    @Override
    public void requestPhoneCode(String phone) {
        long currentTime = System.currentTimeMillis();
        String enc = MD5Util.md5Encrypt32Lower(phone + "jsDyctOCnay7uotq" + currentTime);
        Call<ResponseBody> task = mXxbApi.requestPhoneCode(phone, "86", currentTime + "", enc);
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //数据
                try {
                    ResponseBody body = response.body();
                    String msg = body.string();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(msg);
                    boolean isSuccess = jsonNode.get("status").asBoolean();
                    String info = jsonNode.get("mes").asText();
                    for (ILoginCallback callback : mCallbacks) {
                        if (isSuccess) {
                            callback.onRequestPhoneCodeSuccess(info);
                        } else {
                            callback.onRequestPhoneCodeFailed(info);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    for (ILoginCallback callback : mCallbacks) {
                        callback.onRequestPhoneCodeFailed("未知错误");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
                for (ILoginCallback callback : mCallbacks) {
                    callback.onNetworkError();
                }
            }
        });
    }

    @Override
    public void LoginByCode(String username, String phoneCode) {
        Call<ResponseBody> task = mXxbApi.loginByCode(username, phoneCode);
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //数据
                try {
                    ResponseBody body = response.body();
                    String msg = body.string();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(msg);
                    boolean isSuccess = jsonNode.get("status").asBoolean();
                    String info = jsonNode.get("mes").asText();
                    for (ILoginCallback callback : mCallbacks) {
                        if (isSuccess) {
                            //登录成功
                            //记录账号
                            SPUtil.put(Constants.SP_CONFIG_LOGIN_TYPE, Constants.LOGIN_TYPE_PHONE_CODE, Constants.SP_CONFIG);
                            SPUtil.put("username", username, Constants.SP_CONFIG);
                            //通知UI
                            callback.onLoginByPhoneCodeSuccess(info);
                        } else {
                            callback.onLoginByPhoneCodeFailed(info);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    for (ILoginCallback callback : mCallbacks) {
                        callback.onLoginByPhoneCodeFailed("未知错误");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
                for (ILoginCallback callback : mCallbacks) {
                    callback.onNetworkError();
                }
            }
        });
    }


    @Override
    public void registerViewCallback(ILoginCallback iLoginCallback) {
        if (!mCallbacks.contains(iLoginCallback)) {
            mCallbacks.add(iLoginCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ILoginCallback iLoginCallback) {
        mCallbacks.remove(iLoginCallback);
    }
}

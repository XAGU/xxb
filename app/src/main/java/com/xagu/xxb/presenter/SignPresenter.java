package com.xagu.xxb.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umeng.analytics.MobclickAgent;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.data.XxbApi;
import com.xagu.xxb.interfaces.ISignCallback;
import com.xagu.xxb.interfaces.ISignPresenter;
import com.xagu.xxb.utils.RetrofitManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
public class SignPresenter implements ISignPresenter {

    private volatile static SignPresenter sInstance = null;
    List<ISignCallback> mCallbacks = new ArrayList<>();
    private final XxbApi mXxbApi;


    private SignPresenter() {
        Retrofit retrofit = RetrofitManager.getInstance().getRetrofit();
        mXxbApi = retrofit.create(XxbApi.class);
    }

    public static SignPresenter getInstance() {
        if (sInstance == null) {
            synchronized (SignPresenter.class) {
                if (sInstance == null) {
                    sInstance = new SignPresenter();
                }
            }
        }
        return sInstance;
    }


    @Override
    public void Locationsign(String address, String name, String lng, String lat, String uid, String activeId, String objectId) {
        Call<ResponseBody> task = mXxbApi.sign(address, name, lng, lat, uid, activeId, objectId);
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                try {
                    if (body != null) {
                        for (ISignCallback callback : mCallbacks) {
                            callback.onSignSuccess();
                        }
                    } else {
                        for (ISignCallback callback : mCallbacks) {
                            callback.onSignFail();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    for (ISignCallback callback : mCallbacks) {
                        callback.onSignFail();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
                for (ISignCallback callback : mCallbacks) {
                    callback.onNetWorkError();
                }
            }
        });
    }

    /**
     * 取随机二维码的code
     *
     * @param activeId
     * @return
     */
    public void getQrCode(String activeId) {
        long time = new Date().getTime();
        Call<ResponseBody> task = mXxbApi.getQrCode(activeId, String.valueOf(time));
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                try {
                    if (body != null) {
                        String s = body.string();
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode readTree = objectMapper.readTree(s);
                        String code = readTree.get("msg").asText();
                        for (ISignCallback callback : mCallbacks) {
                            callback.onGetQrCodeSuccess(code);
                        }
                    } else {
                        //获取code失败
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    //获取code失败
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
            }
        });
    }

    private String mPanToken = "";

    @Override
    public void initPanToken() {
        Call<ResponseBody> task = mXxbApi.getPanToken();
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                try {
                    if (body != null) {
                        String s = body.string();
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode readTree = objectMapper.readTree(s);
                        String token = readTree.get("_token").asText();
                        mPanToken = token;
                    } else {
                        //获取code失败
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    //获取code失败
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
            }
        });
    }

    @Override
    public void uploadImg(String imgPath, String uid) {
        File file = new File(imgPath);
        RequestBody requestFile = RequestBody.create(null, file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        Call<ResponseBody> task = mXxbApi.uploadImg(mPanToken, body, uid);
        task.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                try {
                    if (body != null) {
                        String s = body.string();
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode readTree = objectMapper.readTree(s);
                        String objectId = readTree.get("objectId").asText();
                        for (ISignCallback callback : mCallbacks) {
                            callback.onUploadImgSuccess(objectId);
                        }
                    } else {
                        //获取code失败
                        for (ISignCallback callback : mCallbacks) {
                            callback.onUploadImgFail();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MobclickAgent.reportError(BaseApplication.getAppContext(), e);
                    //获取code失败
                    for (ISignCallback callback : mCallbacks) {
                        callback.onUploadImgFail();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //网络错误
                for (ISignCallback callback : mCallbacks) {
                    callback.onUpLoadImgNetError();
                }
            }
        });
    }


    @Override
    public void registerViewCallback(ISignCallback iSignCallback) {
        if (!mCallbacks.contains(iSignCallback)) {
            mCallbacks.add(iSignCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ISignCallback iSignCallback) {
        mCallbacks.remove(iSignCallback);
    }
}

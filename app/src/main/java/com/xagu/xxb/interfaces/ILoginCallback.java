package com.xagu.xxb.interfaces;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface ILoginCallback {
    /**
     * 登录成功
     */
    void onLoginSuccess();

    /**
     * 登录失败
     */
    void onLoginFailed();

    /**
     * 网络错误
     */
    void onNetworkError();


    void onRequestPhoneCodeSuccess(String info);

    void onRequestPhoneCodeFailed(String info);

    void onLoginByPhoneCodeSuccess(String info);

    void onLoginByPhoneCodeFailed(String info);
}

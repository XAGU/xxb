package com.xagu.xxb.interfaces;

import com.xagu.xxb.base.IBasePresenter;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface ILoginPresenter extends IBasePresenter<ILoginCallback> {
    /**
     * 登录
     * @param username
     * @param password
     */
    void Login(String username, String password);

    /**
     * 是否登录
     */
    boolean isLogin();

    void requestPhoneCode(String username);

    void LoginByCode(String username, String phoneCode);
}

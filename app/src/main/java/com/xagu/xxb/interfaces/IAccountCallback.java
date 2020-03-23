package com.xagu.xxb.interfaces;

import com.xagu.xxb.bean.User;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface IAccountCallback {

    /**
     * 查询用户信息成功
     * @param user
     */
    void onGetUserInfoSuccess(User user);

    /**
     * 查询用户信息失败
     */
    void onGetUserInfoFailed();

    /**
     * 查询用户信息网络错误
     */
    void onGetUserInfoNetworkError();


}

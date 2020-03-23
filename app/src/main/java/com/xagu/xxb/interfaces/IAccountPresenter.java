package com.xagu.xxb.interfaces;

import com.xagu.xxb.base.IBasePresenter;
import com.xagu.xxb.bean.User;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface IAccountPresenter extends IBasePresenter<IAccountCallback> {

    /**
     * 获取用户信息
     */
    void getUserInfo();
}

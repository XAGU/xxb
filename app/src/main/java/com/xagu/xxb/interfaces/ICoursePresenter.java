package com.xagu.xxb.interfaces;

import com.xagu.xxb.base.IBasePresenter;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface ICoursePresenter extends IBasePresenter<ICourseCallback> {

    /**
     * 获取用户信息
     */
    void getCourseInfo();
}

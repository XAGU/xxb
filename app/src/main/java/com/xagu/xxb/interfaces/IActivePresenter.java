package com.xagu.xxb.interfaces;

import com.xagu.xxb.base.IBasePresenter;
import com.xagu.xxb.bean.Active;
import com.xagu.xxb.bean.Course;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface IActivePresenter extends IBasePresenter<IActiveCallback> {

    /**
     * 获取用户信息
     */
    void getActiveInfo();

    /**
     * 设置目标Course
     * @param course
     */
    void setTargetCourse(Course course);

    /**
     * 请求active的详细信息
     * @param url
     */
    void requestActiveSignStatus(Active active);

    /**
     * 请求active的类型
     * @param url
     */
    void requestActiveType(Active active);

    /**
     * 添加监控
     */
    void sub();

    /**
     * 取消监控
     */
    void unSub();
}

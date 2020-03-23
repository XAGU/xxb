package com.xagu.xxb.base;

import com.xagu.xxb.bean.Course;

/**
 * Created by XAGU on 2020/2/28
 * Email:xagu_qc@foxmail.com
 * Describe:
 */
public interface IBasePresenter<T> {
    /**
     * 这个方法用于注册ui的回调
     * @param t
     */
    void registerViewCallback(T t);

    /**
     * 取消UI的回调注册
     * @param t
     */
    void unRegisterViewCallback(T t);


}

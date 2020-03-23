package com.xagu.xxb.interfaces;

import com.xagu.xxb.bean.Active;
import com.xagu.xxb.bean.Course;

import java.util.List;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface IActiveCallback {

    /**
     * 查询活动信息成功
     * @param Activity
     */
    void onGetActiveSuccess(List<Active> active);

    /**
     * 查询活动信息失败
     */
    void onGetActiveFailed();

    /**
     * 查询活动信息网络错误
     */
    void onGetActiveNetworkError();

    /**
     * 加载course数据
     * @param course
     */
    void onCourseLoaded(Course course);

    void onRequestActiveDetailSuccess(Active active);
    void onRequestActiveDetailFailed();
    void onRequestActiveDetailNetworkError();


}

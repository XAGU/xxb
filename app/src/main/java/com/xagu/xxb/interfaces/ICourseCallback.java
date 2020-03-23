package com.xagu.xxb.interfaces;

import com.xagu.xxb.bean.Course;
import com.xagu.xxb.bean.User;

import java.util.List;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface ICourseCallback {

    /**
     * 查询用户信息成功
     * @param course
     */
    void onGetCourseSuccess(List<Course> course);

    /**
     * 查询课程信息失败
     */
    void onGetCourseFailed();

    /**
     * 查询课程信息网络错误
     */
    void onGetCourseNetworkError();


}

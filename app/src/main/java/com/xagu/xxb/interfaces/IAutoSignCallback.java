package com.xagu.xxb.interfaces;

import com.xagu.xxb.bean.Course;

import java.util.List;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface IAutoSignCallback {


    void onGetAutoSignCourseEmpty();

    void onGetAutoSignCourseSuccess(List<Course> courses);


    void signLog(int count, int signSuccess);
}

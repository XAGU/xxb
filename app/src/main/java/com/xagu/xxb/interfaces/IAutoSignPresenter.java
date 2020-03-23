package com.xagu.xxb.interfaces;

import com.xagu.xxb.base.IBasePresenter;
import com.xagu.xxb.bean.Active;
import com.xagu.xxb.bean.Course;

import java.util.List;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface IAutoSignPresenter extends IBasePresenter<IAutoSignCallback> {


    void getAutoSignCourse();

    void autoSign(List<Course> course);
}

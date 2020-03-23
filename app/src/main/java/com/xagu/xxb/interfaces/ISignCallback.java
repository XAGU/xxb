package com.xagu.xxb.interfaces;

import com.xagu.xxb.bean.Active;
import com.xagu.xxb.bean.Course;

import java.util.List;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface ISignCallback {

    /**
     * 签到成功
     */
    void onSignSuccess();

    /**
     * 签到失败
     */
    void onSignFail();

    /**
     * 网络错误
     */
   void onNetWorkError();


    /**
     * 获取code成功
     * @param code
     */
    void onGetQrCodeSuccess(String code);

    /**
     * 上传成功
     * @param objectId
     */
    void onUploadImgSuccess(String objectId);

    /**
     * 上传失败
     */
    void onUploadImgFail();

    /**
     * 网络错误
     */
    void onUpLoadImgNetError();
}

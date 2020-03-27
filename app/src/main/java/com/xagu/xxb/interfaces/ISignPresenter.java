package com.xagu.xxb.interfaces;

import com.xagu.xxb.base.IBasePresenter;

/**
 * Created by XAGU on 2020/3/13
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public interface ISignPresenter extends IBasePresenter<ISignCallback> {

    /**
     * 签到
     *  @param address
     * @param name
     * @param lng
     * @param lat
     * @param objectId
     */
    void Locationsign(String address, String name, String lng, String lat, String uid, String activeId, String objectId);


    /**
     *
     * @param activeId
     */
    void getQrCode(String activeId);

    /**
     * 获取panToken
     */
    void initPanToken();

    void uploadImg(String filepath,String filename,String uid);
}

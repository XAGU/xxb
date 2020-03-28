package com.xagu.xxb.utils;

/**
 * Created by XAGU on 2020/2/25
 * Email:xagu_qc@foxmail.com
 * Describe:
 */
public class Constants {

    public final static String SP_CONFIG = "config";
    public static final String SP_SUB_SIGN = "subSign";
    /**
     * 签到类型
     */
    public static final String SIGN_TYPE_GESTURE = "手势签到";
    public static final String TYPE_SIGN_PHOTO = "拍照签到";
    public static final String TYPE_SIGN_COMMON = "普通签到";
    public static final String TYPE_SIGN_LOCATION = "位置签到";
    public static final String TYPE_SIGN_QR = "二维码签到";
    public static final String TYPE_SIGN_RANDOM_QR = "随机二维码签到";
    public static final String TYPE_SIGN_UNKNOWN = "签到";
    /**
     * 签到状态
     */
    public static final String SIGN_STATUS_SUCCESS = "签到成功";
    public static final String URL_SIGN_QR_CODE = "http://qcode.16q.cn:80/code.jspx?w=400&h=400&e=h&url=";

    public static final String SP_CONFIG_SIGN_ADDRESS = "sign_address";
    public static final String SP_CONFIG_SIGN_NAME = "sign_name";
    public static final String SP_CONFIG_SIGN_LNG = "sign_lng";
    public static final String SP_CONFIG_SIGN_LAT = "sign_lat";
    public static final String SP_CONFIG_SIGN_PHOTO = "sign_photo";
    public static final String SP_CONFIG_SIGN_DELAY = "Sign_delay";
    //检查
    public static final int AUTO_SIGN_CHECK_SUCCESS = 1;
    //登录类型
    public static final String SP_CONFIG_LOGIN_TYPE = "loginType";
    //通过验证码登录
    public static final String LOGIN_TYPE_PHONE_CODE = "phoneCode";
    //通过密码登录
    public static final String LOGIN_TYPE_PASSWORD = "password";
}

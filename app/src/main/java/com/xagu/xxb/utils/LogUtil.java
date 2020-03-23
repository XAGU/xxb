package com.xagu.xxb.utils;

import android.util.Log;

/**
 * Created by XAGU on 2020/2/25
 * Email:xagu_qc@foxmail.com
 * Describe: 封装的日志工具类
 */
public class LogUtil {

    public static String sTAG = "LogUtil";

    //控制是否输出log
    public static boolean sIsRelease = false;

    /**
     * 如果要是发布了，可以在application里面把这里release一下，这样就没有log输出了
     * @param baseTag
     * @param isRelease
     */
    public static void init(String baseTag, boolean isRelease){
        sTAG = baseTag;
        sIsRelease = isRelease;
    }

    public static void d(String TAG, String context){
        if (!sIsRelease){
            Log.d("LogUtil[" + sTAG + "]" + TAG, context);
        }
    }

    public static void v(String TAG, String context){
        if (!sIsRelease){
            Log.v("LogUtil[" + sTAG + "]" + TAG, context);
        }
    }

    public static void i(String TAG, String context){
        if (!sIsRelease){
            Log.i("LogUtil[" + sTAG + "]" + TAG, context);
        }
    }

    public static void e(String TAG, String context){
        if (!sIsRelease){
            Log.e("LogUtil[" + sTAG + "]" + TAG, context);
        }
    }
}

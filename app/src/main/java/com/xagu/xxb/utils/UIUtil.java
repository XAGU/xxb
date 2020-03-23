package com.xagu.xxb.utils;

import android.content.Context;

/**
 * Created by XAGU on 2020/3/15
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class UIUtil {
    public static int dip2px(Context context, double dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * (double)density + 0.5D);
    }
}

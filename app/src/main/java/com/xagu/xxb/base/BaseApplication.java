package com.xagu.xxb.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.xagu.xxb.utils.LogUtil;
import com.xagu.xxb.utils.SPUtil;

public class BaseApplication extends Application {

    private static Handler sHandler = null;
    private static Context sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

        //----------------UMENG------
        UMConfigure.init(this,"5e760e16570df3b61d0001fb","yaohuo",0,"XAGU");
        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        //UMConfigure.setLogEnabled(false);
        //----------------UMENG------

        //初始化LogUtil
        LogUtil.init(this.getPackageName(),true);

        //初始化Sp
        SPUtil.init(this);

        sHandler = new Handler();
    }

    public static Handler getsHandler(){
        return sHandler;
    }

    public static Context getAppContext(){
        return sContext;
    }
}

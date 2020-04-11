package com.xagu.xxb;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.xagu.xxb.base.BaseActivity;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.SPUtil;
import com.xagu.xxb.views.DialogFactory;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;

public class WebViewActivity extends BaseActivity {

    private WebView mWvMain;
    private TextView mTvTopBar;
    private ImageView mIvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initView();
        initEvent();
        initWebView();
    }

    private void initWebView() {
        //声明WebSettings子类
        WebSettings webSettings = mWvMain.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

        mWvMain.setWebViewClient(new MyWebViewClient());
        mWvMain.setWebChromeClient(new MyWebChromeClient());
        mWvMain.addJavascriptInterface(this, "androidjsbridge");
        WebView.setWebContentsDebuggingEnabled(true);
        //加载网络url
        syncCookie(mWvMain);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        mWvMain.loadUrl(url);
    }

    private void initEvent() {
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWvMain.canGoBack()) {
                    mWvMain.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    private void initView() {
        mWvMain = findViewById(R.id.wv_container);
        mTvTopBar = findViewById(R.id.tv_top_bar);
        mIvBack = findViewById(R.id.iv_back);
    }


    /**
     * 注意事项：
     * 1.如果需要传第三方cookie，请调用方法setAcceptThirdPartyCookies
     * 2.如果这里有多个cookie，不要使用分号手动拼接，请多次调用setCookie方法
     * 3.请在调用loadUrl方法前执行
     *
     * <p>
     *
     * @param webView
     */
    public void syncCookie(WebView webView) {
        try {
            //CookieSyncManager.createInstance(this);
            // 获取单例CookieManager实例
            CookieManager cookieManager = CookieManager.getInstance();
            // 设置应用程序的WebView实例是否应发送和接受cookie
            cookieManager.setAcceptCookie(true);
            if (webView != null) {
                // 设置是否WebView应允许设置第三方cookie
                cookieManager.setAcceptThirdPartyCookies(webView, true);
            }
            // 删除所有会话cookie
            cookieManager.removeSessionCookies(null);
            // 删除所有cookie
            // cookieManager.removeAllCookies(null);
            List<Cookie> cookies = new SharedPrefsCookiePersistor(this).loadAll();
            // 设置原生cookie
            if (cookies != null && cookies.size() > 0) {
                for (int i = 0; i < cookies.size(); i++) {
                    Cookie cookie = cookies.get(i);
                    cookieManager.setCookie(".chaoxing.com", cookie.toString());
                }
            }
            // 设置第三方cookie
            //cookieManager.setCookie(url, String.format("这里填写cookie的名称=%s", "这里填写cookie的值"),null);
            // 确保当前可通过getCookie API访问的所有cookie都写入持久存储
            //cookieManager.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncCookieToOkHttp(WebView webView) {
        try {
            //CookieSyncManager.createInstance(this);
            // 获取单例CookieManager实例
            CookieManager cookieManager = CookieManager.getInstance();
            String cookieStr = cookieManager.getCookie("chaoxing.com").replaceAll(" ", "");
            List<Cookie> cookies = new ArrayList<>();
            String[] stringStrList = cookieStr.split(";");
            for (String s : stringStrList) {
                String[] split = s.split("=");
                Cookie cookie = new Cookie.Builder().domain("chaoxing.com")
                        .name(split[0])
                        .value(split[1])
                        .build();
                cookies.add(cookie);
            }
            new SharedPrefsCookiePersistor(this).saveAll(cookies);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //view.loadUrl(request.getUrl().toString());
            //作业答案
            String url = request.getUrl().toString();
            if (url.contains("doHomeWork")) {
                //https://mooc1-api.chaoxing.com:443/work/phone/task-work?taskrefId=7578583&courseId=210701456&classId=22038121
                url = url.replace("doHomeWork", "selectWorkQuestionYiPiYue");
                view.loadUrl(url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("doHomeWork")) {
                //https://mooc1-api.chaoxing.com:443/work/phone/task-work?taskrefId=7578583&courseId=210701456&classId=22038121
                url = url.replace("doHomeWork", "selectWorkQuestionYiPiYue");
                view.loadUrl(url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        //ws://47.94.112.76:27025

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mWvMain.setVisibility(View.INVISIBLE);
            showLoadingDialog();
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mWvMain.loadUrl("javascript:(function() {" +
                    "javascript:jsBridge.device = 'android';" +
                    "$(\"#header > div > div.left_arrow\").hide();" +
                    "$(\"body > div.main > h1\").hide();" +
                    "$(\"#ctitle\").css('visibility','hidden');" +
                    "uid = '135683238';})()");
            if (url.contains("mooc1-api.chaoxing.com/work/phone/task-work")) {
                //添加打回作业
                mWvMain.loadUrl("javascript: (function($){\n" +
                        "\t\t$.getUrlParam = function(name){\n" +
                        "\t\t\tvar reg = new RegExp(\"(^|&)\"+ name +\"=([^&]*)(&|$)\");\n" +
                        "\t\t\tvar r = window.location.search.substr(1).match(reg);\n" +
                        "\t\t\tif (r!=null) return unescape(r[2]); return null;\n" +
                        "\t\t}\n" +
                        "\t})(jQuery);\n" +
                        "if($(\"body > div > p > span\").text() == \"分\"){\n" +
                        "\t$(\"body > div > div > p\").after(\"<span class='BlueBtn' onclick='rework()'>打回重做</span>\");   \n" +
                        "\tfunction rework() {\n" +
                        "\t\tvar values = redo.toString().match(/(?<=\")\\d*?(?=\")/g);\n" +
                        "\t\tvar workRelationId = values[0];\n" +
                        "\t\tvar classId = values[1];\n" +
                        "\t\tvar relationAnswerId = values[2];\n" +
                        "\t\tvar courseId = values[3];\n" +
                        "\t\tvar studentId = $.getUrlParam('cpi');\n" +
                        "\t\t$(\".cx_alert-txt\").html(\"确认要打回作业吗?\");\n" +
                        "\t\t$(\"#okBtn\").html(\"打回\");\n" +
                        "\t\t$(\".cx_alert\").css(\"display\", \"block\");\n" +
                        "\t\t$(\".cx_alert-box\").css(\"display\", \"block\");\n" +
                        "\t\t$(\"#okBtn\").unbind();\n" +
                        "\t\t$(\"#cancelBtn\").unbind();\n" +
                        "\t\t$(\"#okBtn\").on(\"click\", function() {\n" +
                        "\t\t\t$.ajax({\n" +
                        "\t\t\t\ttype : \"get\",\n" +
                        "\t\t\t\turl : \"/work/phone/reWork\",\n" +
                        "\t\t\t\tdataType : \"json\",\n" +
                        "\t\t\t\tdata : {\n" +
                        "\t\t\t\t\t\"workRelationId\" : workRelationId,\n" +
                        "\t\t\t\t\t\"classId\" : classId,\n" +
                        "\t\t\t\t\t\"courseId\" : courseId,\n" +
                        "\t\t\t\t\t\"relationAnswerId\" : relationAnswerId,\n" +
                        "\t\t\t\t\t\"studentId\" : studentId\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\tsuccess : function(data) {\n" +
                        "\t\t\t\t\tif(data.status == true) {\n" +
                        "\t\t\t\t\t\t$(\".cx_alert\").css(\"display\", \"none\");\n" +
                        "\t\t\t\t\t\t$(\".cx_alert-box\").css(\"display\", \"none\");\n" +
                        "\t\t\t\t\t\tjsBridge.postNotification('CLIENT_REWORK_SUCCESS', data.msg);\n" +
                        "\t\t\t\t\t} else {\n" +
                        "\t\t\t\t\t$(\".cx_alert\").css(\"display\", \"none\");\n" +
                        "\t\t\t\t\t\t$(\".cx_alert\").css(\"display\", \"none\");\n" +
                        "\t\t\t\t\t\t$(\".cx_alert-box\").css(\"display\", \"none\");\n" +
                        "\t\t\t\t\t\topenWindowHintClient(1, data.msg, null, 1000);\n" +
                        "\t\t\t\t\t}\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t});\n" +
                        "\t\t});\n" +
                        "\t\t\n" +
                        "\t\t$(\"#cancelBtn\").on(\"click\", function() {\n" +
                        "\t\t\t$(\".cx_alert\").css(\"display\", \"none\");\n" +
                        "\t\t\t$(\".cx_alert-box\").css(\"display\", \"none\");\n" +
                        "\t\t});\n" +
                        "\t}\n" +
                        "}\n" +
                        "if($(\"body > div.startBtn > input[type=button]\").attr(\"value\")==\"查看详情\"||$(\"body > div > div.BtmCon > span\").text()==\"查看详情\"){\n" +
                        "\t$(\"body > div.startBtn > input[type=button]\").before(\"<span class='BlueBtn' onclick='chooseDone()'>延长时间</span><br>\");\n" +
                        "\t$(\"body > div > div.BtmCon > span\").before(\"<span class='BlueBtn' onclick='chooseDone()'>延长时间</span>\");\n" +
                        "\t    \n" +
                        "    var script = document.createElement('script');\n" +
                        "    script.src = \"/js/work/phone/iosSelectRem.js?v=2018-0704-1618\";\n" +
                        "    document.getElementsByTagName('head')[0].appendChild(script);\n" +
                        "\n" +
                        "    let linkElm = document.createElement('link');\n" +
                        "    linkElm.setAttribute('rel', 'stylesheet');\n" +
                        "    linkElm.setAttribute('type', 'text/css');\n" +
                        "    linkElm.setAttribute('href', '/css/work/phone/iosSelectRem.css?v=2018-0704-1618');\n" +
                        "    document.head.appendChild(linkElm);\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "    function chooseDone() {\n" +
                        "    \tshowTime();\n" +
                        "\n" +
                        "    }\n" +
                        "    var now = new Date();\n" +
                        "    var nowYear = now.getFullYear();\n" +
                        "    var nowMonth = now.getMonth() + 1;\n" +
                        "    var nowDate = now.getDate();\n" +
                        "    var nowHour = now.getHours();\n" +
                        "    var nowMinute = now.getMinutes();\n" +
                        "    function formatYear (nowYear) {\n" +
                        "    \tvar arr = [];\n" +
                        "        for (var i = nowYear; i <= nowYear + 10; i++) {\n" +
                        "    \t\tarr.push({\n" +
                        "    \t\t\tid: i + '',\n" +
                        "    \t\t\tvalue: i + '年'\n" +
                        "    \t\t});\n" +
                        "    \t}\n" +
                        "    \treturn arr;\n" +
                        "    }\n" +
                        "    function formatMonth () {\n" +
                        "    \tvar arr = [];\n" +
                        "    \tfor (var i = 1; i <= 12; i++) {\n" +
                        "    \t\tarr.push({\n" +
                        "    \t\t\tid: i + '',\n" +
                        "    \t\t\tvalue: i + '月'\n" +
                        "    \t\t});\n" +
                        "    \t}\n" +
                        "    \treturn arr;\n" +
                        "    }\n" +
                        "    function formatDate (count) {\n" +
                        "    \tvar arr = [];\n" +
                        "    \tfor (var i = 1; i <= count; i++) {\n" +
                        "    \t\tarr.push({\n" +
                        "    \t\t\tid: i + '',\n" +
                        "    \t\t\tvalue: i + '日'\n" +
                        "    \t\t});\n" +
                        "    \t}\n" +
                        "    \treturn arr;\n" +
                        "    }\n" +
                        "    var yearData = function(callback) {\n" +
                        "    \tcallback(formatYear(nowYear))\n" +
                        "    };\n" +
                        "    var monthData = function (year, callback) {\n" +
                        "    \tcallback(formatMonth());\n" +
                        "    };\n" +
                        "    var dateData = function (year, month, callback) {\n" +
                        "    \tif (/^(1|3|5|7|8|10|12)$/.test(month)) {\n" +
                        "    \t\tcallback(formatDate(31));\n" +
                        "    \t}\n" +
                        "    \telse if (/^(4|6|9|11)$/.test(month)) {\n" +
                        "    \t\tcallback(formatDate(30));\n" +
                        "    \t}\n" +
                        "    \telse if (/^2$/.test(month)) {\n" +
                        "    \t\tif (year % 4 === 0 && year % 100 !==0 || year % 400 === 0) {\n" +
                        "    \t\t\tcallback(formatDate(29));\n" +
                        "    \t\t}\n" +
                        "    \t\telse {\n" +
                        "    \t\t\tcallback(formatDate(28));\n" +
                        "    \t\t}\n" +
                        "    \t}\n" +
                        "    \telse {\n" +
                        "    \t\tconsole.log(month);\n" +
                        "    \t\tthrow new Error('month is illegal');\n" +
                        "    \t}\n" +
                        "    };\n" +
                        "    var hourData = function(one, two, three, callback) {\n" +
                        "    \tvar hours = [];\n" +
                        "    \tfor (var i = 0,len = 24; i < len; i++) {\n" +
                        "    \t\thours.push({\n" +
                        "    \t\t\tid: i,\n" +
                        "    \t\t\tvalue: i + '时'\n" +
                        "    \t\t});\n" +
                        "    \t}\n" +
                        "    \tcallback(hours);\n" +
                        "    };\n" +
                        "    var minuteData = function(one, two, three, four, callback) {\n" +
                        "    \tvar minutes = [];\n" +
                        "    \tfor (var i = 0, len = 60; i < len; i++) {\n" +
                        "    \t\tminutes.push({\n" +
                        "    \t\t\tid: i,\n" +
                        "    \t\t\tvalue: i + '分'\n" +
                        "    \t\t});\n" +
                        "    \t}\n" +
                        "    \tcallback(minutes);\n" +
                        "    };\n" +
                        "\n" +
                        "    var myendtime = '';\n" +
                        "\n" +
                        "    function showTime() {\n" +
                        "    \tvar oneLevelId = nowYear;\n" +
                        "    \tvar twoLevelId = nowMonth;\n" +
                        "    \tvar threeLevelId = nowDate;\n" +
                        "    \tvar fourLevelId = nowHour;\n" +
                        "    \tvar fiveLevelId = nowMinute;\n" +
                        "    \tvar iosSelect = new IosSelect(5, [yearData, monthData, dateData, hourData, minuteData], {\n" +
                        "    \t\ttitle : '加时',\n" +
                        "    \t\titemHeight : 0.6786,\n" +
                        "    \t\theaderHeight : 0.819,\n" +
                        "    \t\trelation : [1, 1, 0, 0],\n" +
                        "    \t\titemShowCount : 7,\n" +
                        "    \t\tcssUnit : 'rem',\n" +
                        "    \t\toneLevelId : oneLevelId,\n" +
                        "    \t\ttwoLevelId : twoLevelId,\n" +
                        "    \t\tthreeLevelId : threeLevelId,\n" +
                        "    \t\tfourLevelId : fourLevelId,\n" +
                        "    \t\tfiveLevelId : fiveLevelId,\n" +
                        "    \t\tcallback : function(selectOneObj, selectTwoObj, selectThreeObj, selectFourObj, selectFiveObj) {\n" +
                        "    \t\t\tnowYear = selectOneObj.id;\n" +
                        "    \t\t\tnowMonth = selectTwoObj.id;\n" +
                        "    \t\t\tnowDate = selectThreeObj.id;\n" +
                        "    \t\t\tnowHour = selectFourObj.id;\n" +
                        "    \t\t\tnowMinute = selectFiveObj.id;\n" +
                        "\n" +
                        "    \t\t\tvar yt = selectOneObj.id;\n" +
                        "    \t\t\tvar Mt = selectTwoObj.id;\n" +
                        "    \t\t\tMt = addZero(Mt);\n" +
                        "\n" +
                        "    \t\t\tvar dt = selectThreeObj.id;\n" +
                        "    \t\t\tdt = addZero(dt);\n" +
                        "\n" +
                        "    \t\t\tvar Ht = selectFourObj.id;\n" +
                        "    \t\t\tHt = addZero(Ht);\n" +
                        "\n" +
                        "    \t\t\tvar mt = selectFiveObj.id;\n" +
                        "    \t\t\tmt = addZero(mt);\n" +
                        "\n" +
                        "    \t\t\tmyendtime = yt + \"-\" + Mt + \"-\" + dt + \" \" + Ht + \":\" + mt;\n" +
                        "    \t\t\taddTime();\n" +
                        "    \t\t}\n" +
                        "    \t});\n" +
                        "    }\n" +
                        "\n" +
                        "    function addZero(i) {\n" +
                        "    \tif (i < 10) {\n" +
                        "    \t\ti = \"0\" + i;\n" +
                        "    \t}\n" +
                        "    \treturn i;\n" +
                        "    }\n" +
                        "\n" +
                        "    function getCurrentTime() {\n" +
                        "    \tvar now = new Date();\n" +
                        "    \tvar year = now.getFullYear();\n" +
                        "    \tvar month = now.getMonth() + 1;\n" +
                        "    \tmonth = addZero(month);\n" +
                        "    \tvar date = now.getDate();\n" +
                        "    \tdate = addZero(date);\n" +
                        "        var hour = now.getHours();\n" +
                        "        hour = addZero(hour);\n" +
                        "        var minute = now.getMinutes();\n" +
                        "        minute = addZero(minute);\n" +
                        "        var time = year + \"-\" + month + \"-\" + date + \" \" + hour + \":\" + minute + \":00\";\n" +
                        "    \treturn time;\n" +
                        "    }\n" +
                        "    function addTime() {\n" +
                        "    \tvar extraTime = myendtime;\n" +
                        "    \tif (extraTime.length == 0) {\n" +
                        "\t\t\tjsBridge.postNotification('CLIENT_REWORK_SUCCESS', \"请设置加时时间!\");\n" +
                        "    \t\treturn;\n" +
                        "    \t}\n" +
                        "    \textraTime = extraTime + \":00\";\n" +
                        "\n" +
                        "    \tvar nowTime = getCurrentTime();\n" +
                        "    \tif(nowTime >= extraTime) {\n" +
                        "\t\t\tjsBridge.postNotification('CLIENT_REWORK_SUCCESS', \"加时需大于当前时间！\");\n" +
                        "    \t\treturn;\n" +
                        "    \t}\n" +
                        "\n" +
                        "    \tvar endTime = $(\"#endTime\").val();\n" +
                        "    \tif(endTime >= extraTime) {\n" +
                        "\t\t\tjsBridge.postNotification('CLIENT_REWORK_SUCCESS', \"加时需大于截止时间！\");\n" +
                        "    \t\treturn;\n" +
                        "    \t}\n" +
                        "\n" +
                        "    \tvar workId = $.getUrlParam('taskrefId');\n" +
                        "    \tvar ids = $.getUrlParam('cpi');\n" +
                        "    \tvar classId = $.getUrlParam('classId');\n" +
                        "    \tvar courseId = $.getUrlParam('courseId');\n" +
                        "    \t$(\".cx_alert-txt\").html(\"确认要延长作业时间吗?\");\n" +
                        "    \t$(\"#okBtn\").html(\"延长\");\n" +
                        "    \t$(\".cx_alert\").css(\"display\", \"block\");\n" +
                        "    \t$(\".cx_alert-box\").css(\"display\", \"block\");\n" +
                        "    \t$(\"#okBtn\").unbind();\n" +
                        "    \t$(\"#cancelBtn\").unbind();\n" +
                        "    \t$(\"#okBtn\").on(\"click\", function() {\n" +
                        "    \t\t$.ajax({\n" +
                        "    \t\t\ttype : \"get\",\n" +
                        "    \t\t\turl : \"/work/add-time\",\n" +
                        "    \t\t\tdataType : \"json\",\n" +
                        "    \t\t\tdata : {\n" +
                        "    \t\t\t\t\"ids\" : ids,\n" +
                        "    \t\t\t\t\"time\" : extraTime,\n" +
                        "    \t\t\t\t\"workId\" : workId,\n" +
                        "    \t\t\t\t\"classId\" : classId,\n" +
                        "    \t\t\t\t\"courseId\" : courseId\n" +
                        "    \t\t\t},\n" +
                        "    \t\t\tsuccess : function(data) {\n" +
                        "    \t\t\t\tif(data.status == true) {\n" +
                        "    \t\t\t\t\t$(\".cx_alert\").css(\"display\", \"none\");\n" +
                        "    \t\t\t\t\t$(\".cx_alert-box\").css(\"display\", \"none\");\n" +
                        "    \t\t\t\t\tjsBridge.postNotification('CLIENT_REWORK_SUCCESS', data.msg);\n" +
                        "    \t\t\t\t} else {\n" +
                        "    \t\t\t\t$(\".cx_alert\").css(\"display\", \"none\");\n" +
                        "\t\t\t\t\t\t$(\".cx_alert\").css(\"display\", \"none\");\n" +
                        "    \t\t\t\t\t$(\".cx_alert-box\").css(\"display\", \"none\");\n" +
                        "    \t\t\t\t\tjsBridge.postNotification('CLIENT_REWORK_SUCCESS', data.msg);\n" +
                        "    \t\t\t\t}\n" +
                        "    \t\t\t}\n" +
                        "    \t\t});\n" +
                        "    \t});\n" +
                        "\n" +
                        "    \t$(\"#cancelBtn\").on(\"click\", function() {\n" +
                        "    \t\t$(\".cx_alert\").css(\"display\", \"none\");\n" +
                        "    \t\t$(\".cx_alert-box\").css(\"display\", \"none\");\n" +
                        "    \t});\n" +
                        "    }\n" +
                        "}");
            }
            BaseApplication.getsHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWvMain.setVisibility(View.VISIBLE);
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
            }, 300);
            super.onPageFinished(view, url);
        }
    }

    private Dialog mDialog = null;

    private void showLoadingDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = DialogFactory.creatRequestDialog(this, "加载中...");
        mDialog.show();
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            mTvTopBar.setText(title);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWvMain.canGoBack()) {
            mWvMain.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @JavascriptInterface
    public void postNotification(String name, String msg) {
        ObjectMapper objectMapper = new ObjectMapper();
        switch (name) {
            case "CLIENT_OPEN_URL":
                try {
                    JsonNode jsonNode = objectMapper.readTree(msg);
                    String webUrl = jsonNode.get("webUrl").asText();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mWvMain.loadUrl(webUrl);
                        }
                    });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                break;
            case "CLIENT_THIRD_LOGIN":
                syncCookieToOkHttp(mWvMain);
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("isFromLogin", true);
                //记录账号
                SPUtil.put(Constants.SP_CONFIG_LOGIN_TYPE, Constants.LOGIN_TYPE_STUDENT_NUM, Constants.SP_CONFIG);
                startActivity(intent);
                finish();
                break;
            case "CLIENT_REFRESH_STATUS":
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WebViewActivity.this, "发布签到成功", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
                break;
            case "CLIENT_SHOW_MESSAGE":
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WebViewActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case "CLIENT_REWORK_SUCCESS":
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWvMain.goBack();
                        mWvMain.reload();
                        Toast.makeText(WebViewActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }
}

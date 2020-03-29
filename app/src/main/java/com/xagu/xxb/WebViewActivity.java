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
        }
    }
}

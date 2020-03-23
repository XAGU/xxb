package com.xagu.xxb;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.xagu.xxb.base.BaseActivity;
import com.xagu.xxb.fragments.AboutFragment;
import com.xagu.xxb.fragments.AutoSignFragment;
import com.xagu.xxb.fragments.CourseFragment;
import com.xagu.xxb.fragments.UserInfoFragment;
import com.xagu.xxb.interfaces.ILoginCallback;
import com.xagu.xxb.interfaces.ILoginPresenter;
import com.xagu.xxb.presenter.AccountPresenter;
import com.xagu.xxb.presenter.LoginPresenter;
import com.xagu.xxb.views.TabEntity;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements ILoginCallback {

    private ArrayList<Fragment> mFragments = new ArrayList<>();

    private String[] mTitles = {"首页", "监控签到", "我","关于"};
    private int[] mIconUnselectIds = {
            R.mipmap.tab_course, R.mipmap.tab_autosign,
            R.mipmap.tab_userinfo,R.mipmap.tab_about};
    private int[] mIconSelectIds = {
            R.mipmap.tab_course_select, R.mipmap.tab_autosign_select,
            R.mipmap.tab_userinfo_select,R.mipmap.tab_about_select};
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private CommonTabLayout mTabLayout;
    private ILoginPresenter mLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initPresenter();
        //判断是否登录
        isLogin();
    }

    private void isLogin() {
        boolean isFromLogin = getIntent().getBooleanExtra("isFromLogin", false);
        if (isFromLogin) {
            //从Login跳转过来的，不用判断登录
            return;
        }
        if (!mLoginPresenter.isLogin()) {
            //未登录,跳转登录
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void initPresenter() {
        mLoginPresenter = LoginPresenter.getInstance();
        mLoginPresenter.registerViewCallback(this);
    }

    private void initView() {
        mFragments.add(new CourseFragment());
        mFragments.add(new AutoSignFragment());
        mFragments.add(new UserInfoFragment());
        mFragments.add(new AboutFragment());
        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }
        mTabLayout = findViewById(R.id.main_tab);
        mTabLayout.setTabData(mTabEntities, this, R.id.fl_container, mFragments);
        //显示未读红点
        //mTabLayout.showDot(2);
    }

    @Override
    public void onLoginSuccess() {
        //啥也不干
    }

    @Override
    public void onLoginFailed() {
        //跳转登录
        Toast.makeText(this, "登录信息已失效", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onNetworkError() {
        Toast.makeText(this, "网络不佳", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoginPresenter != null) {
            mLoginPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);//true对任何Activity都适用
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

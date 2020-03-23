package com.xagu.xxb.views;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.wangnan.library.painter.AliPayPainter;
import com.xagu.xxb.R;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.bean.Active;
import com.xagu.xxb.interfaces.ISignCallback;
import com.xagu.xxb.interfaces.ISignPresenter;
import com.xagu.xxb.presenter.SignPresenter;

/**
 * Created by XAGU on 2020/2/29
 * Email:xagu_qc@foxmail.com
 * Describe:
 */
public class SignGestureWindow extends PopupWindow implements ISignCallback {


    private final View mPopView;
    private final Activity mContext;
    private TextView mTvSignType;
    private TextView mTvSignTime;
    private TextView mTvSign;
    private ValueAnimator mEnterBgAnimation;
    private ValueAnimator mExitBgAnimation;
    private final int BG_ANIMATION_DURATION = 800;
    private Active mActice = null;
    private ISignPresenter mSignPresenter;
    private MyGestureLockView mGtLock;


    public SignGestureWindow(Activity activity) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.mContext = activity;
        //这里要注意，设置setOutsideTouchable(true);这个属性前，先要设置setBackgroundDrawable();否则点击外部无法关闭
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        //载进来View
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.dialog_sign_gesture, null);
        //设置内容
        setContentView(mPopView);
        //设置进入离开弹窗的动画
        setAnimationStyle(R.style.pop_animation);
        initView();

        initPresenter();

        initEvent();
        initBgAnimation();
    }

    private void initPresenter() {
        mSignPresenter = SignPresenter.getInstance();
        mSignPresenter.registerViewCallback(this);
    }


    private void initEvent() {
        mTvSign.setOnClickListener(v -> {
            //签到
            if (mTvSign.getText().equals("签到")) {
                String url = mActice.getUrl();
                String uid = url.substring(url.indexOf("&uid=") + 5, url.lastIndexOf("&"));
                mSignPresenter.Locationsign("", "", "-1", "-1", uid, mActice.getId(), null);
            }
        });
    }


    public void setData(Active active) {
        this.mActice = active;
        mTvSignType.setText(active.getActiveTypeName());
        mTvSignTime.setText(active.getTime());
        if (active.isSigned()) {
            if (Integer.parseInt(active.getStatus()) == 2) {
                mTvSign.setText("已过期&已签到");
            } else {
                mTvSign.setText("已签到");
            }
            mTvSign.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.colorGray));
        } else {
            if (Integer.parseInt(active.getStatus()) == 2) {
                mTvSign.setText("已过期&未签到");
                mTvSign.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.colorGray));
            } else {
                mTvSign.setText("签到");
                mTvSign.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.colorPrimary));
            }
        }
        initGesture();
    }

    private void initView() {
        this.setFocusable(true);
        mTvSignType = mPopView.findViewById(R.id.tv_sign_type);
        mTvSignTime = mPopView.findViewById(R.id.tv_sign_time);
        mTvSign = mPopView.findViewById(R.id.tv_sign);
        mGtLock = mPopView.findViewById(R.id.gesture_lock);
    }

    private void initGesture() {
        mGtLock.setPainter(new AliPayPainter());
        mGtLock.setLockPass(mActice.getSignCode());
    }


    private void initBgAnimation() {
        //进入的
        mEnterBgAnimation = ValueAnimator.ofFloat(1.0f, 0.7f);
        mEnterBgAnimation.setDuration(BG_ANIMATION_DURATION);
        mEnterBgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //处理下背景，有点透明度
                updateBgAlpha((Float) animation.getAnimatedValue());
            }
        });
        //退出的
        mExitBgAnimation = ValueAnimator.ofFloat(0.7f, 1.0f);
        mExitBgAnimation.setDuration(BG_ANIMATION_DURATION);
        mExitBgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //处理下背景，有点透明度
                updateBgAlpha((Float) animation.getAnimatedValue());
            }
        });
    }

    public void updateBgAlpha(float alpha) {
        Window window = mContext.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = alpha;
        window.setAttributes(attributes);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        mSignPresenter.registerViewCallback(this);
        mEnterBgAnimation.start();

    }

    @Override
    public void dismiss() {
        mExitBgAnimation.start();
        mSignPresenter.unRegisterViewCallback(this);
        super.dismiss();
    }

    @Override
    public void onSignSuccess() {
        Toast.makeText(mContext, "签到成功", Toast.LENGTH_SHORT).show();
        mTvSign.setText("已签到");
        mTvSign.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.colorGray));
    }

    @Override
    public void onSignFail() {
        Toast.makeText(mContext, "签到失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetWorkError() {
        Toast.makeText(mContext, "网络错误", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetQrCodeSuccess(String code) {

    }

    @Override
    public void onUploadImgSuccess(String objectId) {

    }

    @Override
    public void onUploadImgFail() {

    }

    @Override
    public void onUpLoadImgNetError() {

    }
}

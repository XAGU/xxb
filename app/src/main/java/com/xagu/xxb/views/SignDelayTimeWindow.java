package com.xagu.xxb.views;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.xagu.xxb.R;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.bean.Active;
import com.xagu.xxb.interfaces.ISignPresenter;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.SPUtil;

/**
 * Created by XAGU on 2020/2/29
 * Email:xagu_qc@foxmail.com
 * Describe:
 */
public class SignDelayTimeWindow extends PopupWindow {


    private final View mPopView;
    private final Activity mContext;
    private TextView mTvSignType;
    private TextView mTvSignTime;
    private TextView mTvSubmit;
    private ValueAnimator mEnterBgAnimation;
    private ValueAnimator mExitBgAnimation;
    private final int BG_ANIMATION_DURATION = 800;
    private Active mActice = null;
    private ISignPresenter mSignPresenter;
    private EditText mEtDelayTime;


    public SignDelayTimeWindow(Activity activity) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.mContext = activity;
        //这里要注意，设置setOutsideTouchable(true);这个属性前，先要设置setBackgroundDrawable();否则点击外部无法关闭
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        //载进来View
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.dialog_sign_delay, null);
        //设置内容
        setContentView(mPopView);
        //设置进入离开弹窗的动画
        setAnimationStyle(R.style.pop_animation);
        initView();

        initEvent();
        initBgAnimation();
    }


    private void initEvent() {
        mTvSubmit.setOnClickListener(v -> {
            String delay = mEtDelayTime.getText().toString();
            if (TextUtils.isEmpty(delay)) {
                Toast.makeText(mContext, "延迟时间不能为空", Toast.LENGTH_SHORT).show();
            } else {
                SPUtil.put(Constants.SP_CONFIG_SIGN_DELAY, delay, Constants.SP_CONFIG);
                dismiss();
            }
        });
    }




    private void initView() {
        this.setFocusable(true);
        mTvSubmit = mPopView.findViewById(R.id.tv_submit);
        mEtDelayTime = mPopView.findViewById(R.id.et_sign_delay);
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
        mEnterBgAnimation.start();

    }

    @Override
    public void dismiss() {
        mExitBgAnimation.start();
        super.dismiss();
    }

}

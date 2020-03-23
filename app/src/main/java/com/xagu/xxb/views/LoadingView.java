package com.xagu.xxb.views;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.xagu.xxb.R;

/**
 * Created by XAGU on 2020/2/27
 * Email:xagu_qc@foxmail.com
 * Describe:
 */
public class LoadingView extends AppCompatImageView {

    //旋转的角度
    private int rotateDegree = 0;
    private boolean mNeedRotate = false;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //设置图片
        setImageResource(R.mipmap.loading_blue);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mNeedRotate = true;
        post(new Runnable() {
            @Override
            public void run() {
                rotateDegree += 15;
                rotateDegree = rotateDegree <= 360 ? rotateDegree : 0;
                invalidate();
                //是否继续旋转
                if (mNeedRotate) {
                    postDelayed(this, 100);
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        mNeedRotate = false;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * 第一个参数是旋转角度
         * 第二个参数是旋转的x坐标
         * 第三个参数是旋转的y坐标
         */
        canvas.rotate(rotateDegree, getWidth() / 2f, getHeight() / 2f);
        super.onDraw(canvas);
    }
}

package com.xagu.xxb.views;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.wangnan.library.GestureLockThumbnailView;
import com.wangnan.library.GestureLockView;
import com.wangnan.library.listener.OnGestureLockListener;
import com.wangnan.library.model.Point;
import com.wangnan.library.painter.Painter;
import com.xagu.xxb.base.BaseApplication;

/**
 * Created by XAGU on 2020/3/16
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class MyGestureLockView extends GestureLockView {

    private int viewSize;
    String number = null;
    private Point[][] mPoints = new Point[3][3];
    private boolean isDrawedPass = false;

    public MyGestureLockView(Context context) {
        this(context, null);
    }

    public MyGestureLockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyGestureLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 1.边长修正（宽高不一致时，以最小值为准）
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        viewSize = Math.min(width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        initPointArray();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * 初始化3x3点数组
     */
    private void initPointArray() {
        for (int i = 0; i < 3; i++) { // i为"行标"
            for (int j = 0; j < 3; j++) { // j为"列标"
                Point point = new Point();
                point.x = (2 * j + 1) * viewSize / 6;
                point.y = (2 * i + 1) * viewSize / 6;
                point.status = Point.POINT_NORMAL_STATUS;
                point.index = i * 3 + j;
                mPoints[i][j] = point;
            }
        }
    }

    public void setLockPass(String number) {
        this.number = number;
    }

    private void drawPass() {
        this.isDrawedPass = true;
        long downTime = SystemClock.uptimeMillis();
        // 0.数字序列合法性判断
        if (TextUtils.isEmpty(number)) {
            return;
        }
        if (number.length() > 9) {
            return;
        }
        if (!number.matches("^\\d+$")) {
            return;
        }
        char[] chars = number.toCharArray();
        int[] numbers = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            numbers[i] = chars[i] - 49;
        }
        Point startPoint = mPoints[numbers[0] / 3][numbers[0] % 3];
        MotionEvent downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, startPoint.x, startPoint.y, 0);
        super.onTouchEvent(downEvent);
        // 3.设置被按下的点状态
        for (int i = 1; i < numbers.length; i++) {
            Point point = mPoints[numbers[i] / 3][numbers[i] % 3];
            final MotionEvent event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_MOVE, point.x, point.y, 0);
            super.onTouchEvent(event);
        }
        Point endPoint = mPoints[numbers[numbers.length - 1] / 3][numbers[numbers.length - 1] % 3];
        MotionEvent upEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_UP, endPoint.x, endPoint.y, 0);
        super.onTouchEvent(upEvent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isDrawedPass) {
            drawPass();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
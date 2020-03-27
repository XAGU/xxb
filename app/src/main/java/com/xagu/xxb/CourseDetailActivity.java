package com.xagu.xxb;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.xagu.xxb.adapter.ActiveListAdapter;
import com.xagu.xxb.base.BaseActivity;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.bean.Active;
import com.xagu.xxb.bean.Course;
import com.xagu.xxb.interfaces.IActiveCallback;
import com.xagu.xxb.presenter.ActivePresenter;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.UIUtil;
import com.xagu.xxb.views.DialogFactory;
import com.xagu.xxb.views.SignAddressWindow;
import com.xagu.xxb.views.SignCommonWindow;
import com.xagu.xxb.views.SignGestureWindow;
import com.xagu.xxb.views.SignPhotoWindow;
import com.xagu.xxb.views.SignQRCodeWindow;
import com.xagu.xxb.views.UILoader;

import java.util.Iterator;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CourseDetailActivity extends BaseActivity implements IActiveCallback {

    private ActivePresenter mActivePresenter;
    private ImageView mIvBigCover;
    private ImageView mIvSmallCover;
    private TextView mTvCourseTitle;
    private TextView mTvCourseAuthor;
    private RecyclerView mRvActiveList;
    private ActiveListAdapter mActiveListAdapter;
    private SignAddressWindow mSignAddressWindow = null;
    private SignQRCodeWindow mSignQRCodeWindow = null;
    private SignGestureWindow mSignGestureWindow = null;
    private SignCommonWindow mSignCommonWindow = null;
    private SignPhotoWindow mSignPhotoWindow = null;
    private TwinklingRefreshLayout mTwRefresh;
    private ViewGroup mDetailListContainer;
    private UILoader mUiLoader = null;
    private TextView mTvSubCourse;
    private ImageView mMClazzScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);
        initView();
        initPresenter();
        initEvent();
    }

    private void initPresenter() {
        mActivePresenter = ActivePresenter.getInstance();
        mActivePresenter.registerViewCallback(this);
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        mActivePresenter.getActiveInfo();
    }

    private void initEvent() {
        mTvSubCourse.setText(mActivePresenter.isSub() ? "取消监控" : "监控");
        mTvSubCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivePresenter.isSub()) {
                    mActivePresenter.unSub();
                } else {
                    mActivePresenter.sub();
                }
                mTvSubCourse.setText(mActivePresenter.isSub() ? "取消监控" : "监控");
            }
        });

        mMClazzScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseDetailActivity.this, WebViewActivity.class);
                Course course = mActivePresenter.getTargetCourse();
                String url = "https://mooc1-api.chaoxing.com/phone/moocAnalysis/analysisScore_new?courseId="
                        + course.getCourseId() + "&classId="
                        + course.getClassId() + "&isWeixin=0";
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        mIvBigCover = findViewById(R.id.iv_large_cover);
        mIvSmallCover = findViewById(R.id.riv_small_cover);
        mTvCourseTitle = findViewById(R.id.tv_course_title);
        mTvCourseAuthor = findViewById(R.id.tv_course_author);
        mTvSubCourse = findViewById(R.id.detail_sub_btn);
        mDetailListContainer = findViewById(R.id.detail_list_container);
        mMClazzScore = findViewById(R.id.iv_clazz_score);
        //
        if (mUiLoader == null) {
            mUiLoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }
            };
            mDetailListContainer.removeAllViews();
            mDetailListContainer.addView(mUiLoader);
            mUiLoader.setOnRetryClickListener(new UILoader.OnRetryClickListener() {
                @Override
                public void onRetryClick() {
                    mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
                    mActivePresenter.getActiveInfo();
                }
            });
        }
    }

    private View createSuccessView(ViewGroup container) {
        View detailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
        mRvActiveList = detailListView.findViewById(R.id.rv_active_list);
        mTwRefresh = detailListView.findViewById(R.id.twink_refresh);
        mTwRefresh.setEnableLoadmore(false);
        mRvActiveList.setLayoutManager(new LinearLayoutManager(this));
        mActiveListAdapter = new ActiveListAdapter();
        ActiveListAdapter activeListAdapter = mActiveListAdapter;
        mRvActiveList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 2);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 2);
                outRect.left = UIUtil.dip2px(view.getContext(), 2);
                outRect.right = UIUtil.dip2px(view.getContext(), 2);
                //super.getItemOffsets(outRect, view, parent, state);
            }
        });
        mRvActiveList.setAdapter(activeListAdapter);
        mTwRefresh.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
                mActivePresenter.getActiveInfo();
            }
        });
        mActiveListAdapter.setOnItemClickListener(new ActiveListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, Active active) {
                /*Intent intent = new Intent(CourseDetailActivity.this,WebViewActivity.class);
                intent.putExtra("url",active.getUrl());
                startActivity(intent);*/
                //请求签到信息
                if (Integer.parseInt(active.getActiveType()) == 42) {
                    //测验
                    Intent intent = new Intent(CourseDetailActivity.this, WebViewActivity.class);
                    String url = "https://mobilelearn.chaoxing.com/pptTestPaperStu/questionChartStatistic?classId="
                            + mActivePresenter.getTargetCourse().getClassId()
                            + "&creatorFlag=1&activePrimaryId=" + active.getId() + "&appType=15";
                    intent.putExtra("url", url);
                    startActivity(intent);
                } else if (Integer.parseInt(active.getActiveType()) == 2) {
                    showLoadingDialog();
                    mActivePresenter.requestActiveType(active);
                } else {
                    Intent intent = new Intent(CourseDetailActivity.this, WebViewActivity.class);
                    intent.putExtra("url", active.getUrl());
                    startActivity(intent);
                }
            }
        });
        return detailListView;
    }

    @Override
    public void onGetActiveSuccess(List<Active> actives) {
        Iterator<Active> iterator = actives.iterator();
        while (iterator.hasNext()) {
            Active active = iterator.next();
            if (Integer.parseInt(active.getActiveType()) != 2) {
                if (Integer.parseInt(active.getActiveType()) != 42) {
                    if (Integer.parseInt(active.getActiveType()) != 19) {
                        //iterator.remove();
                    }
                }
            }
        }
        if (actives.size() == 0) {
            mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
        } else {
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
            mActiveListAdapter.setData(actives);
            mTwRefresh.onFinishRefresh();
            Toast.makeText(this, "加载成功", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetActiveFailed() {
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onGetActiveNetworkError() {
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onCourseLoaded(Course course) {
        //设置数据
        if (mIvBigCover != null) {
            Glide.with(this).load(course.getImageUrl()).apply(RequestOptions.bitmapTransform(new BlurTransformation(14, 3))).into(mIvBigCover);
        }
        if (mIvSmallCover != null) {
            Glide.with(this).load(course.getImageUrl()).into(mIvSmallCover);
        }
        if (mTvCourseTitle != null) {
            mTvCourseTitle.setText(course.getName());
        }
        if (mTvCourseAuthor != null) {
            mTvCourseAuthor.setText(course.getTeacher());
        }
    }

    @Override
    public void onRequestActiveDetailSuccess(Active active) {
        switch (active.getActiveTypeName()) {
            case Constants
                    .TYPE_SIGN_COMMON:
                //普通签到
                if (mSignCommonWindow == null) {
                    mSignCommonWindow = new SignCommonWindow(CourseDetailActivity.this);
                }
                //展示播放列表
                mSignCommonWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                mSignCommonWindow.setData(active);
                break;
            case Constants.SIGN_TYPE_GESTURE:
                if (mSignGestureWindow == null) {
                    mSignGestureWindow = new SignGestureWindow(CourseDetailActivity.this);
                }
                //展示播放列表
                mSignGestureWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                mSignGestureWindow.setData(active);
                break;
            case Constants.TYPE_SIGN_QR:
            case Constants.TYPE_SIGN_RANDOM_QR:
                if (mSignQRCodeWindow == null) {
                    mSignQRCodeWindow = new SignQRCodeWindow(CourseDetailActivity.this);
                }
                //展示播放列表
                mSignQRCodeWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                mSignQRCodeWindow.setData(active);
                break;
            case Constants.TYPE_SIGN_LOCATION:
                CourseDetailActivityPermissionsDispatcher.showLocationPopWindowWithPermissionCheck(this, active);
                break;
            case Constants.TYPE_SIGN_PHOTO:
                if (mSignPhotoWindow == null) {
                    mSignPhotoWindow = new SignPhotoWindow(CourseDetailActivity.this);
                }
                //展示播放列表
                mSignPhotoWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                mSignPhotoWindow.setData(active);
                break;
        }
        BaseApplication.getsHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
            }
        }, 300);

    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    public void showLocationPopWindow(Active active) {
        if (mSignAddressWindow == null) {
            mSignAddressWindow = new SignAddressWindow(CourseDetailActivity.this);
        }
        //展示播放列表
        mSignAddressWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        mSignAddressWindow.setData(active);
    }

    @Override
    public void onRequestActiveDetailFailed() {
        //TODO:失败
    }

    @Override
    public void onRequestActiveDetailNetworkError() {
        //TODO:网络错误
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivePresenter.unRegisterViewCallback(this);
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


    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showDeniedForCamera() {
        Toast.makeText(this, "请授予定位权限！", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showNeverAskForCamera() {
        Toast.makeText(this, "请进入设置开启定位权限~~", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        CourseDetailActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


}

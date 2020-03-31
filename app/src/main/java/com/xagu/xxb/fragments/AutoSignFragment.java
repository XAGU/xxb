package com.xagu.xxb.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xagu.xxb.CourseDetailActivity;
import com.xagu.xxb.R;
import com.xagu.xxb.adapter.CourseListAdapter;
import com.xagu.xxb.base.BaseFragment;
import com.xagu.xxb.bean.Course;
import com.xagu.xxb.interfaces.IAutoSignCallback;
import com.xagu.xxb.presenter.ActivePresenter;
import com.xagu.xxb.presenter.AutoSignPresenter;
import com.xagu.xxb.service.AutoSignService;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.SPUtil;
import com.xagu.xxb.utils.UIUtil;
import com.xagu.xxb.views.SignAddressWindow;
import com.xagu.xxb.views.SignDelayTimeWindow;
import com.xagu.xxb.views.SignPhotoWindow;
import com.xagu.xxb.views.UILoader;

import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by XAGU on 2020/3/14
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
@RuntimePermissions
public class AutoSignFragment extends BaseFragment implements IAutoSignCallback, AutoSignService.AutoSignCallback {

    private View mView;
    private RecyclerView mRvSignCourse;
    private CourseListAdapter mCourseListAdapter;
    private UILoader mUiLoader;
    private TextView mTvSignCourseCount;
    private TextView mTvSignLog;
    private View mLlSignAddress;
    private View mLlSignPhoto;
    private View mLlSignDelay;
    private SignAddressWindow mSignAddressWindow = null;
    private SignPhotoWindow mSignPhotoWindow = null;
    private String mUid;
    private SignDelayTimeWindow mSignDelayWindow = null;
    private TextView mTvSignAddress;
    private TextView mTvSignPhoto;
    private TextView mTvSignDelay;
    private Button mBtnSign;
    private String mAddress;
    private String mPhoto;
    private String mDelay;
    private List<Course> mCourse;
    private AutoSignPresenter mAutoSignPresenter;

    private ServiceConnection mConn;
    private AutoSignService mAutoSignService;
    private Activity mActivity;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        if (mUiLoader == null) {
            if (getActivity() != null) {
                mActivity = getActivity();
            }
            mUiLoader = new UILoader(mActivity) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return CreateSuccessView(layoutInflater, container);
                }

                @Override
                protected View getEmptyView() {
                    View emptyView = super.getEmptyView();
                    ((TextView) emptyView.findViewById(R.id.empty_text)).setText("请先添加一门课程监控后再来吧~~~");
                    return emptyView;
                }
            };
        }
        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
        }
        initView();
        initEvent();
        initPresenter();
        initAutoSignService();
        return mUiLoader;
    }


    private void initAutoSignService() {
        mConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                AutoSignService.MyBinder myBinder = (AutoSignService.MyBinder) binder;
                mAutoSignService = myBinder.getService();
                mAutoSignService.setAutoSignCallback(AutoSignFragment.this);
                mAutoSignService.setCourseData(mCourse);
                mAutoSignService.startAutoSign();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                if (mAutoSignService != null) {
                    mAutoSignService.stopAutoSign();
                    mAutoSignService = null;
                }
            }
        };
    }


    private void initPresenter() {
        mAutoSignPresenter = AutoSignPresenter.getInstance();
        mAutoSignPresenter.registerViewCallback(this);
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        mAutoSignPresenter.getAutoSignCourse();
    }

    private void initView() {
        mTvSignCourseCount = mView.findViewById(R.id.tv_sign_course_count);
        mTvSignLog = mView.findViewById(R.id.tv_sign_log);
        mLlSignAddress = mView.findViewById(R.id.ll_sign_address);
        mTvSignAddress = mView.findViewById(R.id.tv_sign_address);
        mLlSignPhoto = mView.findViewById(R.id.ll_sign_photo);
        mTvSignPhoto = mView.findViewById(R.id.tv_sign_photo);
        mLlSignDelay = mView.findViewById(R.id.ll_sign_delay);
        mTvSignDelay = mView.findViewById(R.id.tv_sign_delay);
        mBtnSign = mView.findViewById(R.id.btn_start_sign);
        initSignConfigStatus();
    }

    private void initSignConfigStatus() {
        //更新
        mAddress = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_ADDRESS, "", Constants.SP_CONFIG);
        mTvSignAddress.setText(mAddress);
        mPhoto = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_PHOTO, "", Constants.SP_CONFIG);
        mTvSignPhoto.setText(TextUtils.isEmpty(mPhoto) ? "未设置" : "已设置");
        mDelay = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_DELAY, "", Constants.SP_CONFIG);
        mTvSignDelay.setText(mDelay);
    }

    private View CreateSuccessView(LayoutInflater layoutInflater, ViewGroup container) {
        mView = layoutInflater.inflate(R.layout.fragment_autosign, container, false);
        mRvSignCourse = mView.findViewById(R.id.rv_sign_course);
        mRvSignCourse.setLayoutManager(new LinearLayoutManager(mActivity));
        mRvSignCourse.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 8);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 8);
                outRect.left = UIUtil.dip2px(view.getContext(), 8);
                outRect.right = UIUtil.dip2px(view.getContext(), 8);
                //super.getItemOffsets(outRect, view, parent, state);
            }
        });
        mCourseListAdapter = new CourseListAdapter();
        mRvSignCourse.setAdapter(mCourseListAdapter);
        return mView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAutoSignPresenter != null) {
            mAutoSignPresenter.unRegisterViewCallback(this);
        }
        if (mAutoSignService != null) {
            mActivity.unbindService(mConn);
        }
    }

    private void initEvent() {
        mCourseListAdapter.setOnItemClickListener(new CourseListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, Course course) {
                ActivePresenter.getInstance().setTargetCourse(course);
                startActivity(new Intent(mActivity, CourseDetailActivity.class));
            }
        });

        mLlSignAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoSignFragmentPermissionsDispatcher.showAddressWindowWithPermissionCheck(AutoSignFragment.this);
            }
        });

        mLlSignPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSignPhotoWindow == null) {
                    mSignPhotoWindow = new SignPhotoWindow(mActivity);
                }
                //展示播放列表
                mSignPhotoWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                mSignPhotoWindow.setAutoSignOption(mUid);
                mSignPhotoWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        //更新
                        String photo = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_PHOTO, "", Constants.SP_CONFIG);
                        mTvSignPhoto.setText(TextUtils.isEmpty(photo) ? "未设置" : "已设置");

                    }
                });
            }
        });

        mLlSignDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSignDelayWindow == null) {
                    mSignDelayWindow = new SignDelayTimeWindow(mActivity);
                }
                //展示播放列表
                mSignDelayWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                mSignDelayWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        //更新
                        String delay = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_DELAY, "", Constants.SP_CONFIG);
                        mTvSignDelay.setText(delay);
                    }
                });
            }
        });


        //开始签到
        mBtnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAutoSignService == null || !mAutoSignService.isRun()) {
                    //没有运行，点击开始运行
                    Intent service = new Intent(mActivity, AutoSignService.class);
                    mActivity.bindService(service, mConn, Service.BIND_AUTO_CREATE);
                } else {
                    mAutoSignService.stopAutoSign();
                    mActivity.unbindService(mConn);
                    mAutoSignService = null;
                }
            }
        });
    }


    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    public void showAddressWindow() {
        if (mSignAddressWindow == null) {
            mSignAddressWindow = new SignAddressWindow(mActivity);
        }
        mSignAddressWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        mSignAddressWindow.setAutoSignOption();
        mSignAddressWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                String address = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_ADDRESS, "", Constants.SP_CONFIG);
                mTvSignAddress.setText(address);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAutoSignService != null) {
            mAutoSignService.setAutoSignCallback(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAutoSignService != null) {
            mAutoSignService.removeAutoSignCallback();
        }
    }

    @Override
    public void onGetAutoSignCourseEmpty() {
        mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
    }

    @Override
    public void onGetAutoSignCourseSuccess(List<Course> courses) {
        mCourse = courses;
        if (mCourseListAdapter != null) {
            if (mUid == null) {
                this.mUid = courses.get(0).getUid();
            }
            mCourseListAdapter.setData(courses);
            mTvSignCourseCount.setText("共监控课程" + courses.size() + "门");
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
    }

    @Override
    public void signLog(int count, int signSuccess) {
        mTvSignLog.setText("已运行检测" + count + "次，成功签到" + signSuccess + "次");
    }


    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showDeniedForCamera() {
        Toast.makeText(mActivity, "请授予定位权限！", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showNeverAskForCamera() {
        Toast.makeText(mActivity, "请进入设置开启定位权限~~", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoSignFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * 开启服务成功
     */
    @Override
    public void onStartAutoSign() {
        mBtnSign.setText("停止监控");
        mBtnSign.setBackground(mActivity.getDrawable(R.drawable.bg_btn_red));
    }

    /**
     * 停止服务成功
     */
    @Override
    public void onStopAutoSign() {
        mBtnSign.setText("开始");
        mBtnSign.setBackground(mActivity.getDrawable(R.drawable.bg_btn));
    }

    /**
     * 日志
     *
     * @param sign
     * @param signSuccess
     */
    @Override
    public void onSignLog(int sign, int signSuccess) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvSignLog.setText("已运行检测" + sign + "次，成功签到" + signSuccess + "次");
            }
        });
    }
}

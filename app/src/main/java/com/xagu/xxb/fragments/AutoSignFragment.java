package com.xagu.xxb.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
public class AutoSignFragment extends BaseFragment implements IAutoSignCallback {

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
    private List<Course> mCourse = new ArrayList<>();
    private LocalBroadcastManager mManager;
    private AutoSignPresenter mAutoSignPresenter;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        if (mUiLoader == null) {
            mUiLoader = new UILoader(getActivity()) {
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

    private void initPresenter() {
        mAutoSignPresenter = AutoSignPresenter.getInstance();
        mAutoSignPresenter.registerViewCallback(this);
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        mAutoSignPresenter.getAutoSignCourse();
    }

    private void initAutoSignService() {
        //获取局部广播管理员
        mManager = LocalBroadcastManager.getInstance(getActivity());
        //注册局部广播
        IntentFilter filter = new IntentFilter("autoSign");
        mManager.registerReceiver(receiver, filter);
    }

    //广播接受者
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取广播消息
            int signCount = intent.getIntExtra("signCount", 0);
            int signSuccess = intent.getIntExtra("signSuccess", 0);
            mTvSignLog.setText("已运行检测" + signCount + "次，成功签到" + signSuccess + "次");
        }
    };

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
        mRvSignCourse.setLayoutManager(new LinearLayoutManager(getActivity()));
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
        mManager.unregisterReceiver(receiver);
    }

    private void initEvent() {
        mCourseListAdapter.setOnItemClickListener(new CourseListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, Course course) {
                ActivePresenter.getInstance().setTargetCourse(course);
                startActivity(new Intent(getActivity(), CourseDetailActivity.class));
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
                    mSignPhotoWindow = new SignPhotoWindow(getActivity());
                }
                //展示播放列表
                mSignPhotoWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
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
                    mSignDelayWindow = new SignDelayTimeWindow(getActivity());
                }
                //展示播放列表
                mSignDelayWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
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
                if (isRun) {
                    //mAutoSignPresenter.stopSign();
                    if (getActivity() != null) {
                        Intent service = new Intent(getActivity(), AutoSignService.class);
                        getActivity().stopService(service);
                    }
                    mBtnSign.setText("开始");
                    mBtnSign.setBackground(getActivity().getDrawable(R.drawable.bg_btn));
                } else {
                    if (getActivity() != null) {
                        Intent service = new Intent(getActivity(), AutoSignService.class);
                        service.putExtra("courses", (Serializable) mCourse);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getActivity().startForegroundService(service);
                        } else {
                            getActivity().startService(service);
                        }
                    }
                    //mAutoSignPresenter.autoSign(mCourse);
                    mBtnSign.setText("停止监控");
                    mBtnSign.setBackground(getActivity().getDrawable(R.drawable.bg_btn_red));
                }
                isRun = !isRun;
            }
        });
    }
    boolean isRun = false;

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    public void showAddressWindow() {
        if (mSignAddressWindow == null) {
            mSignAddressWindow = new SignAddressWindow(getActivity());
        }
        mSignAddressWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
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
    public void onGetAutoSignCourseEmpty() {
        mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
    }

    @Override
    public void onGetAutoSignCourseSuccess(List<Course> courses) {
        mCourse.addAll(courses);
        if (mCourseListAdapter != null) {
            this.mUid = courses.get(0).getUid();
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
        Toast.makeText(getActivity(), "请授予定位权限！", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showNeverAskForCamera() {
        Toast.makeText(getActivity(), "请进入设置开启定位权限~~", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoSignFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}

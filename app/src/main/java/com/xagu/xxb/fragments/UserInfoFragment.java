package com.xagu.xxb.fragments;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xagu.xxb.LoginActivity;
import com.xagu.xxb.R;
import com.xagu.xxb.base.BaseFragment;
import com.xagu.xxb.bean.User;
import com.xagu.xxb.interfaces.IAccountCallback;
import com.xagu.xxb.interfaces.IAccountPresenter;
import com.xagu.xxb.presenter.AccountPresenter;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.SPUtil;
import com.xagu.xxb.views.CircleImageView;

/**
 * Created by XAGU on 2020/3/14
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class UserInfoFragment extends BaseFragment implements IAccountCallback {

    private View mView;
    private Button mBtnLogout;
    private TextView mTvInfoUID;
    private TextView mTvInfoStudentID;
    private TextView mTvInfoUsername;
    private TextView mTvInfoPhone;
    private TextView mTvInfoSchool;
    private TextView mTvInfoEmail;
    private CircleImageView mCiHeadPic;
    private IAccountPresenter mAccountPresenter;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        mView = layoutInflater.inflate(R.layout.fragment_userinfo, container, false);
        if (mView.getParent() instanceof ViewGroup) {
            ((ViewGroup) mView.getParent()).removeView(mView);
        }
        initView();
        initPresenter();
        initEvent();
        return mView;
    }

    private void initPresenter() {
        mAccountPresenter = AccountPresenter.getInstance();
        mAccountPresenter.registerViewCallback(this);
        mAccountPresenter.getUserInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAccountPresenter != null) {
            mAccountPresenter.unRegisterViewCallback(this);
        }
    }

    private void initEvent() {
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtil.clear(Constants.SP_CONFIG);
                SPUtil.clear(Constants.SP_SUB_SIGN);
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });
    }

    private void initView() {
        mBtnLogout = mView.findViewById(R.id.btn_logout);
        mTvInfoUID = mView.findViewById(R.id.tv_uid);
        mTvInfoStudentID = mView.findViewById(R.id.tv_student_id);
        mTvInfoUsername = mView.findViewById(R.id.tv_username);
        mTvInfoPhone = mView.findViewById(R.id.tv_phone);
        mTvInfoSchool = mView.findViewById(R.id.tv_school_name);
        mTvInfoEmail = mView.findViewById(R.id.tv_email);
        mCiHeadPic = mView.findViewById(R.id.ci_head);
    }

    @Override
    public void onGetUserInfoSuccess(User user) {
        //成功
        if (user != null) {
            setData(user);
        }
    }

    private void setData(User user) {
        String uid = user.getUid();
        if (!TextUtils.isEmpty(uid)) {
            mTvInfoUID.setText(uid);
        }
        String studentId = user.getStudentId();
        if (!TextUtils.isEmpty(studentId)) {
            mTvInfoStudentID.setText(studentId);
        }
        String nickname = user.getNickname();
        if (!TextUtils.isEmpty(nickname)) {
            mTvInfoUsername.setText(nickname);
        }
        String phoneNum = user.getPhoneNum();
        if (!TextUtils.isEmpty(phoneNum)) {
            mTvInfoPhone.setText(phoneNum);
        }
        String schoolName = user.getSchoolName();
        if (!TextUtils.isEmpty(schoolName)) {
            mTvInfoSchool.setText(schoolName);
        }
        String email = user.getEmail();
        if (!TextUtils.isEmpty(email)) {
            mTvInfoEmail.setText(email);
        }
        String headPic = user.getHeadPic();
        if (!TextUtils.isEmpty(headPic) && getContext() != null) {
            Glide.with(getContext()).load(headPic).error(getContext().getDrawable(R.mipmap.head_pic_error)).into(mCiHeadPic);
        }
    }

    @Override
    public void onGetUserInfoFailed() {
        //失败
    }

    @Override
    public void onGetUserInfoNetworkError() {
        //网络错误
    }
}

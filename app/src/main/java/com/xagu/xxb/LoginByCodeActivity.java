package com.xagu.xxb;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xagu.xxb.base.BaseActivity;
import com.xagu.xxb.interfaces.ILoginCallback;
import com.xagu.xxb.presenter.LoginPresenter;
import com.xagu.xxb.views.DialogFactory;

public class LoginByCodeActivity extends BaseActivity implements ILoginCallback {

    private EditText mEtPhoneCode;
    private EditText mEtPhone;
    private Button mBtnLogin;
    private TextView mTvRequestPhoneCode;
    private LoginPresenter mLoginPresenter;
    private ImageView mIvDeleteInput;
    private ImageView mIvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_by_code);
        initView();
        initEvent();
        //初始化presenter
        initPresenter();
    }


    private void initPresenter() {
        mLoginPresenter = LoginPresenter.getInstance();
        mLoginPresenter.registerViewCallback(this);
    }

    private void initView() {
        mEtPhone = findViewById(R.id.et_phone);
        mEtPhoneCode = findViewById(R.id.et_phone_code);
        mBtnLogin = findViewById(R.id.btn_login);
        mTvRequestPhoneCode = findViewById(R.id.tv_request_phone_code);
        mIvDeleteInput = findViewById(R.id.iv_input_delete);
        mIvBack = findViewById(R.id.iv_back);
        //Glide.with(this).load("http://passport2.chaoxing.com/createqr?uuid="+UUID.randomUUID()).into(mIvQrCode);
    }

    private void initEvent() {
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mEtPhone.getText().toString().trim();
                String phoneCode = mEtPhoneCode.getText().toString().trim();
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(phoneCode)) {
                    showLoadingDialog();
                    mLoginPresenter.LoginByCode(username, phoneCode);
                }
            }
        });
        mEtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mIvDeleteInput.setVisibility(View.INVISIBLE);
                } else {
                    mIvDeleteInput.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mIvDeleteInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtPhone.setText("");
            }
        });
        mTvRequestPhoneCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //请求验证码
                String username = mEtPhone.getText().toString().trim();
                if (!TextUtils.isEmpty(username)) {
                    mLoginPresenter.requestPhoneCode(username);
                } else {
                    Toast.makeText(LoginByCodeActivity.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onLoginSuccess() {
    }

    @Override
    public void onLoginFailed() {
    }

    @Override
    public void onNetworkError() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPhoneCodeSuccess(String info) {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        new PhoneCodeCountDownTimer(60000, 1000).start();
    }

    @Override
    public void onRequestPhoneCodeFailed(String info) {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginByPhoneCodeSuccess(String info) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("isFromLogin", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoginByPhoneCodeFailed(String info) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }


    private Dialog mDialog = null;

    private void showLoadingDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = DialogFactory.creatRequestDialog(this, "登陆中...");
        mDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoginPresenter != null) {
            mLoginPresenter.unRegisterViewCallback(this);
        }
    }

    //复写倒计时
    private class PhoneCodeCountDownTimer extends CountDownTimer {

        public PhoneCodeCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        //计时过程
        @Override
        public void onTick(long l) {
            //防止计时过程中重复点击
            mTvRequestPhoneCode.setClickable(false);
            mTvRequestPhoneCode.setTextColor(getResources().getColor(R.color.colorGray));
            mTvRequestPhoneCode.setText(l / 1000 + "s后重新获取");

        }

        //计时完毕的方法
        @Override
        public void onFinish() {
            //重新给Button设置文字
            mTvRequestPhoneCode.setText("重新获取验证码");
            mTvRequestPhoneCode.setTextColor(getResources().getColor(R.color.colorPrimaryText));
            //设置可点击
            mTvRequestPhoneCode.setClickable(true);
        }
    }
}

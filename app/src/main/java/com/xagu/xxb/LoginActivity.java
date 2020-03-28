package com.xagu.xxb;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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

public class LoginActivity extends BaseActivity implements ILoginCallback {

    private EditText mEtPassword;
    private EditText mEtUsername;
    private Button mBtnLogin;
    private LoginPresenter mLoginPresenter;
    private ImageView mIvQrCode;
    private ImageView mIvDeleteInput;
    private ImageView mIvPassVisibility;
    private TextView mTvPhoneCodeLogin;
    private TextView mTvRegister;
    private TextView mTvForgotPass;
    private TextView mTvOtherLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
        mEtUsername = findViewById(R.id.et_username);
        mEtPassword = findViewById(R.id.et_phone_code);
        mBtnLogin = findViewById(R.id.btn_login);
        mTvPhoneCodeLogin = findViewById(R.id.tv_phone_code_login);
        mIvQrCode = findViewById(R.id.iv_qr_code);
        mIvDeleteInput = findViewById(R.id.iv_input_delete);
        mIvPassVisibility = findViewById(R.id.iv_pass_visibility);
        mTvRegister = findViewById(R.id.tv_register);
        mTvForgotPass = findViewById(R.id.tv_forgot_pass);
        mTvOtherLogin = findViewById(R.id.tv_other_login);
        //Glide.with(this).load("http://passport2.chaoxing.com/createqr?uuid="+UUID.randomUUID()).into(mIvQrCode);
    }

    private void initEvent() {
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mEtUsername.getText().toString().trim();
                String password = mEtPassword.getText().toString().trim();
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                    showLoadingDialog();
                    mLoginPresenter.Login(username,password);
                }
            }
        });
        mEtUsername.addTextChangedListener(new TextWatcher() {
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
                mEtUsername.setText("");
            }
        });

        mIvPassVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtPassword.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                    //不可见状态，点击设置成可见
                    mIvPassVisibility.setImageResource(R.mipmap.eye_open);
                    mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mEtPassword.setSelection(mEtPassword.length());
                } else {
                    //可见状态，点击设置成不可见
                    mIvPassVisibility.setImageResource(R.mipmap.eye_close);
                    mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mEtPassword.setSelection(mEtPassword.length());
                }
            }
        });

        mTvPhoneCodeLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,LoginByCodeActivity.class));
            }
        });

        mTvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,WebViewActivity.class);
                intent.putExtra("url","https://passport2-api.chaoxing.com:443/v11/register");
                startActivity(intent);
            }
        });

        mTvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,WebViewActivity.class);
                intent.putExtra("url","https://passport2-api.chaoxing.com:443/pwd/getpwdforapp?t=1&v=9");
                startActivity(intent);
            }
        });

        mTvOtherLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,WebViewActivity.class);
                intent.putExtra("url","https://passport2-api.chaoxing.com:443/v6/login");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onLoginSuccess() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isFromLogin",true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoginFailed() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show();
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

    }

    @Override
    public void onRequestPhoneCodeFailed(String info) {

    }

    @Override
    public void onLoginByPhoneCodeSuccess(String info) {

    }

    @Override
    public void onLoginByPhoneCodeFailed(String info) {

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
}

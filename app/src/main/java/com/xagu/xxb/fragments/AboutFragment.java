package com.xagu.xxb.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xagu.xxb.R;
import com.xagu.xxb.base.BaseFragment;

/**
 * Created by XAGU on 2020/3/14
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class AboutFragment extends BaseFragment {

    private View mView;
    private View mRlEmail;
    private View mMRlQQ;
    private View mMRlUpdate;
    private TextView mTvVersion;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        mView = layoutInflater.inflate(R.layout.fragment_about_me, container, false);
        if (mView.getParent() instanceof ViewGroup) {
            ((ViewGroup) mView.getParent()).removeView(mView);
        }
        initView();
        initEvent();
        return mView;
    }

    private void initEvent() {
        mRlEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 必须明确使用mailto前缀来修饰邮件地址,如果使用
                // intent.putExtra(Intent.EXTRA_EMAIL, email)，结果将匹配不到任何应用
                Uri uri = Uri.parse("mailto:xagu_qc@foxmail.com");
                String[] email = {"xagu_qc@foxmail.com"};
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
                intent.putExtra(Intent.EXTRA_SUBJECT, ""); // 主题
                intent.putExtra(Intent.EXTRA_TEXT, ""); // 正文
                startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
            }
        });
        mMRlQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinQQGroup("_kpDlVOPhFCisc5Qur_wgI9H091XVrKq");
            }
        });
        mMRlUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://www.lanzous.com/b08r5r7za");
                intent.setData(content_url);
                startActivity(intent);
            }
        });
    }


    /****************
     *
     * 发起添加群流程。群号：湖北免流  一个健康的(247117453) 的 key 为： jONDgzlMsFEN-WKCr5OCFc1q6lrWP7pm
     * 调用 joinQQGroup(jONDgzlMsFEN-WKCr5OCFc1q6lrWP7pm) 即可发起手Q客户端申请加群 湖北免流  一个健康的(247117453)
     *群号：学习崩(489215136) 的 key 为： _kpDlVOPhFCisc5Qur_wgI9H091XVrKq
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }


    private void initView() {
        mRlEmail = mView.findViewById(R.id.rl_email_container);
        mMRlQQ = mView.findViewById(R.id.rl_about_qq);
        mMRlUpdate = mView.findViewById(R.id.rl_about_update);
        mTvVersion = mView.findViewById(R.id.tv_app_version);
        mTvVersion.setText("Version "+getAppVersionName(getActivity()));
    }

    /**
     * 获取当前app version name
     */
    public static String getAppVersionName(Context context) {
        String appVersionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            appVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionName;
    }
}

package com.xagu.xxb.views;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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


import androidx.annotation.RequiresApi;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.xagu.xxb.R;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.bean.Active;
import com.xagu.xxb.interfaces.ISignCallback;
import com.xagu.xxb.interfaces.ISignPresenter;
import com.xagu.xxb.presenter.SignPresenter;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.SPUtil;

/**
 * Created by XAGU on 2020/2/29
 * Email:xagu_qc@foxmail.com
 * Describe:
 */
public class SignAddressWindow extends PopupWindow implements AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener, ISignCallback {


    private final View mPopView;
    private final Activity mContext;
    private TextView mTvSignType;
    private TextView mTvSignTime;
    private EditText mEtAddress;
    private EditText mEtName;
    private EditText mEtLng;
    private EditText mEtLat;
    private TextView mTvSign;
    private ValueAnimator mEnterBgAnimation;
    private ValueAnimator mExitBgAnimation;
    public final int BG_ANIMATION_DURATION = 800;
    public MapView mGdMap;
    private AMap mAMap;
    private UiSettings mUiSettings;//定义一个UiSettings对象
    private Marker locationMarker;
    private GeocodeSearch geocoderSearch;
    private Active mActice = null;
    private ISignPresenter mSignPresenter;

    public SignAddressWindow(Activity activity) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.mContext = activity;
        //这里要注意，设置setOutsideTouchable(true);这个属性前，先要设置setBackgroundDrawable();否则点击外部无法关闭
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        //载进来View
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.dialog_sign_location, null);
        //设置内容
        setContentView(mPopView);
        //设置进入离开弹窗的动画
        setAnimationStyle(R.style.pop_animation);
        initView();
        //初始化高德地图
        initGDMap();

        initPresenter();

        initEvent();
        initBgAnimation();
    }

    private void initPresenter() {
        mSignPresenter = SignPresenter.getInstance();
    }

    private void initGDMap() {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mGdMap.onCreate(null);
        MyLocationStyle myLocationStyle = new MyLocationStyle().myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        //初始化地图控制器对象
        mAMap = mGdMap.getMap();

        mUiSettings = mAMap.getUiSettings();

        mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮

        mAMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置

        mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style

        mAMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {

                LatLng latLng = mAMap.getCameraPosition().target;
                Point screenPosition = mAMap.getProjection().toScreenLocation(latLng);
                locationMarker = mAMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f));
                /*.icon(BitmapDescriptorFactory.fromResource(R.drawable.bqdw_icon)));*/
                //设置Marker在屏幕上,不跟随地图移动
                locationMarker.setPositionByPixels(screenPosition.x, screenPosition.y);
                locationMarker.showInfoWindow();
            }
        });

        mAMap.setOnCameraChangeListener(this);
        if (geocoderSearch == null) {
            geocoderSearch = new GeocodeSearch(mContext);
        }
        geocoderSearch.setOnGeocodeSearchListener(this);

        //设置希望展示的地图缩放级别
        CameraUpdate mCameraUpdate = CameraUpdateFactory.zoomTo(17);
        mAMap.moveCamera(mCameraUpdate);
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        mAMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }


    private void initEvent() {
        mTvSign.setOnClickListener(v -> {
            //经度偏移0.00634‬  纬度偏移0.00634‬
            float lng = Float.parseFloat(mEtLng.getText().toString()) + 0.00634f;
            float lat = Float.parseFloat(mEtLat.getText().toString()) + 0.00634f;
            //签到
            if (mTvSign.getText().equals("签到")) {
                String url = mActice.getUrl();
                //经度偏移0.00634‬  纬度偏移0.00634‬
                String uid = url.substring(url.indexOf("&uid=")+5, url.lastIndexOf("&"));
                mSignPresenter.Locationsign(mEtAddress.getText().toString(), mEtName.getText().toString(), lng + "", lat + "", uid, mActice.getId(), null);
            } else if (mTvSign.getText().equals("确定")){
                //mEtAddress.getText().toString(), mEtName.getText().toString(), lng + "", lat + ""
                SPUtil.put(Constants.SP_CONFIG_SIGN_ADDRESS,mEtAddress.getText().toString(), Constants.SP_CONFIG);
                SPUtil.put(Constants.SP_CONFIG_SIGN_NAME,mEtName.getText().toString(), Constants.SP_CONFIG);
                SPUtil.put(Constants.SP_CONFIG_SIGN_LNG,lng+"", Constants.SP_CONFIG);
                SPUtil.put(Constants.SP_CONFIG_SIGN_LAT,lat+"", Constants.SP_CONFIG);
                dismiss();
            }
        });
    }

    public void setAutoSignOption(){
        mTvSign.setText("确定");
    }



    public void setData(Active active) {
        this.mActice = active;
        mTvSignType.setText(active.getActiveTypeName());
        mTvSignTime.setText(active.getTime());
        if (active.isSigned()) {
            if (Integer.parseInt(active.getStatus()) == 2) {
                mTvSign.setText("已过期&已签到");
            } else {
                mTvSign.setText("已签到");
            }
            mTvSign.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.colorGray));
        } else {
            if (Integer.parseInt(active.getStatus()) == 2) {
                mTvSign.setText("已过期&未签到");
                mTvSign.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.colorGray));
            } else {
                mTvSign.setText("签到");
                mTvSign.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    private void initView() {
        this.setFocusable(true);
        mTvSignType = mPopView.findViewById(R.id.tv_sign_type);
        mTvSignTime = mPopView.findViewById(R.id.tv_sign_time);
        mEtAddress = mPopView.findViewById(R.id.et_sign_address);
        mEtName = mPopView.findViewById(R.id.et_sign_name);
        mEtLng = mPopView.findViewById(R.id.et_sign_lng);
        mEtLat = mPopView.findViewById(R.id.et_sign_lat);
        mTvSign = mPopView.findViewById(R.id.tv_sign);
        //获取地图控件引用
        mGdMap = mPopView.findViewById(R.id.gd_map);
    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        LatLng target = cameraPosition.target;
        mEtLng.setText(target.longitude + "");
        mEtLat.setText(target.latitude + "");

        LatLonPoint latLonPoint = new LatLonPoint(target.latitude, target.longitude);
        //逆地理编码，通过经纬度获取地理位置
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);//逆向对象，下一步给初始化
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int code) {
        if (code == 1000 && regeocodeResult != null) {
            //逆向地理地址
            String address = regeocodeResult.getRegeocodeAddress().getFormatAddress();
            if (!TextUtils.isEmpty(address)) {
                mEtAddress.setText(address);
                mEtAddress.setSelection(address.length());
            }
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int code) {

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
        mSignPresenter.registerViewCallback(this);
        mEnterBgAnimation.start();
        mGdMap.onResume();

    }

    @Override
    public void dismiss() {
        mExitBgAnimation.start();
        mGdMap.onPause();
        mSignPresenter.unRegisterViewCallback(this);
        super.dismiss();
    }

    private static final int REQUEST_SMS_PERMISSION = 1;


    @Override
    public void onSignSuccess() {
        Toast.makeText(mContext, "签到成功", Toast.LENGTH_SHORT).show();
        mTvSign.setText("已签到");
        mTvSign.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.colorGray));
    }

    @Override
    public void onSignFail() {
        Toast.makeText(mContext, "签到失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetWorkError() {
        Toast.makeText(mContext, "网络错误", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetQrCodeSuccess(String code) {

    }

    @Override
    public void onUploadImgSuccess(String objectId) {

    }

    @Override
    public void onUploadImgFail() {

    }

    @Override
    public void onUpLoadImgNetError() {

    }

}

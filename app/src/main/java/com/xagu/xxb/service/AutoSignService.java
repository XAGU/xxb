package com.xagu.xxb.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xagu.xxb.MainActivity;
import com.xagu.xxb.R;
import com.xagu.xxb.bean.Active;
import com.xagu.xxb.bean.Course;
import com.xagu.xxb.data.XxbApi;
import com.xagu.xxb.utils.Constants;
import com.xagu.xxb.utils.RetrofitManager;
import com.xagu.xxb.utils.SPUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by XAGU on 2020/3/23
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class AutoSignService extends Service {

    private List<Course> mCourses;
    private XxbApi mXxbApi;
    private boolean mIsRun = false;
    int count = 0;
    int signSuccess = 0;
    private AutoSignCallback mCallback;
    private Runnable mRunnable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public AutoSignService getService() {
            return AutoSignService.this;
        }
    }


    @Override
    public void onCreate() {
        setForegroundNotification();
        Retrofit retrofit = RetrofitManager.getInstance().getRetrofit();
        mXxbApi = retrofit.create(XxbApi.class);
        //获取活动
        mRunnable = new Runnable() {
            @Override
            public void run() {
                String address = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_ADDRESS, "", Constants.SP_CONFIG);
                String lng = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_LNG, "-1", Constants.SP_CONFIG);
                String lat = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_LAT, "-1", Constants.SP_CONFIG);
                String name = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_NAME, "", Constants.SP_CONFIG);
                String photo = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_PHOTO, "", Constants.SP_CONFIG);
                String delay = (String) SPUtil.get(Constants.SP_CONFIG_SIGN_DELAY, "0", Constants.SP_CONFIG);
                while (mIsRun) {
                    for (Course course1 : mCourses) {
                        List<Active> actives = getActiveInfo(course1);
                        if (actives == null || actives.size() == 0) {
                            //没有任务
                            continue;
                        }
                        for (Active active : actives) {
                            if (requestActiveSignStatus(active)) {
                                //没签到的
                                if (sign(address, name, lng, lat, course1.getUid(), active.getId(), photo)) {
                                    //成功签到，通知ui,发送广播
                                    ++signSuccess;
                                    sendNotification(course1.getName());
                                    if (mCallback != null) {
                                        mCallback.onSignLog(count, signSuccess);
                                    }
                                }
                            }
                        }
                    }
                    //通知ui增加次数
                    ++count;
                    if (mCallback != null) {
                        mCallback.onSignLog(count, signSuccess);
                    }
                    try {
                        Thread.sleep(Integer.parseInt(delay));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mIsRun = false;
        super.onDestroy();
    }

    public void setAutoSignCallback(AutoSignCallback callback) {//注意这里以单个回调为例  如果是向多个activity传送数据 可以定义一个回调集合 在此处进行集合的添加
        this.mCallback = callback;
    }

    public void removeAutoSignCallback() {
        this.mCallback = null;
    }

    // 通过回调机制，将Service内部的变化传递到外部
    public interface AutoSignCallback {
        void onStartAutoSign();

        void onStopAutoSign();

        void onSignLog(int sign, int signSuccess);
    }


    public void startAutoSign() {
        if (mCourses != null) {
            mIsRun = true;
            new Thread(mRunnable).start();
            mCallback.onStartAutoSign();
        }
    }

    public void stopAutoSign() {
        mIsRun = false;
        mCallback.onStopAutoSign();
    }

    public boolean isRun() {
        return mIsRun;
    }

    private void setForegroundNotification() {
        // 在API11之后构建Notification的方式
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);
        nfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.mipmap.ic_launcher))// 设置下拉列表中的图标(大图标)

                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("正在监控中.....") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            String CHANNEL_ONE_ID = "com.xagu.xxb";
            String CHANNEL_ONE_NAME = "Channel One";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(true);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }
        startForeground(1, notification);
    }

    void sendNotification(String courseName) {
        String CHANNEL_ONE_ID = "com.xagu.xxb";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ONE_ID); //获取一个Notification构造器
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ONE_NAME = "Channel One";
            //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(true);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }
        // 在API11之后构建Notification的方式
        Intent nfIntent = new Intent(this, MainActivity.class);
        nfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.mipmap.ic_launcher))// 设置下拉列表中的图标(大图标)
                .setContentTitle("签到成功") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("课程：" + courseName) // 设置上下文内容
                .setDefaults(NotificationCompat.DEFAULT_ALL)  //使用默认效果， 会根据手机当前环境播放铃声， 是否振动
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setTicker("签到成功")
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        int randomId = (int) (Math.random() * 100);
        manager.notify(randomId, notification);
    }


    public boolean sign(String address, String name, String lng, String lat, String uid, String activeId, String objectId) {
        Call<ResponseBody> task = mXxbApi.sign(address, name, lng, lat, uid, activeId, objectId);
        try {
            Response<ResponseBody> response = task.execute();
            ResponseBody body = response.body();
            if (body != null) {
                //成功
                return true;
            } else {
                return false;
                //失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private List<Active> getActiveInfo(Course course) {
        String courseId = course.getCourseId();
        String classId = course.getClassId();
        String uid = course.getUid();
        Call<ResponseBody> task = mXxbApi.getActiveInfo(courseId, classId, uid);
        try {
            Response<ResponseBody> response = task.execute();
            ResponseBody body = response.body();
            if (body != null) {
                String s = body.string();
                //筛选已经签到的
                return analysisSignActiveInfo(s);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 解析查询Course返回的json
     *
     * @param response
     */
    private List<Active> analysisSignActiveInfo(String response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Active> actives = new ArrayList<>();
        JsonNode readTree = objectMapper.readTree(response);
        JsonNode activeList = readTree.get("activeList");
        for (JsonNode jsonNode : activeList) {
            String status;
            if (jsonNode.has("status")) {
                status = jsonNode.get("status").asText();
            } else {
                continue;
            }
            String activeType;
            if (jsonNode.has("activeType")) {
                activeType = jsonNode.get("activeType").asText();
            } else {
                continue;
            }
            if (activeType.equals("19")) {
                continue;
            }
            if (!status.equals("1")) {
                break;
            }
            Active active = new Active();
            active.setStatus(status);
            active.setActiveType(activeType);
            if (jsonNode.has("id")) {
                active.setId(jsonNode.get("id").asText());
            } else {
                active.setId("");
            }
            //后台签到不需要签到名字
/*            if (jsonNode.has("nameOne")) {
                active.setName(jsonNode.get("nameOne").asText());
            } else {
                active.setName("");
            }*/
            //后台签到不需要结束时间
            /*if (jsonNode.has("nameFour")) {
                active.setTime(jsonNode.get("nameFour").asText());
            } else {
                active.setTime("");
            }*/
            if (jsonNode.has("url")) {
                active.setUrl(jsonNode.get("url").asText());
            } else {
                active.setUrl("");
            }
            /*if (jsonNode.has("picUrl")) {
                active.setCover_url(jsonNode.get("picUrl").asText());
            } else {
                active.setCover_url("");
            }*/
            actives.add(active);
        }
        return actives;
    }

    public void setCourseData(List<Course> courses) {
        mCourses = courses;
    }

    private boolean requestActiveSignStatus(Active active) {
        Call<ResponseBody> task = mXxbApi.checkSignStatus(active.getUrl());
        try {
            Response<ResponseBody> response = task.execute();
            Document doc = Jsoup.parse(response.body().string());
            if (Constants.SIGN_STATUS_SUCCESS.equals(doc.select("#statuscontent").text())) {
                //签到了
                return false;
            } else {
                //没签到
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

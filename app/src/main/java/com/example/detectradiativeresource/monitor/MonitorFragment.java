package com.example.detectradiativeresource.monitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.R;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.detectradiativeresource.Test.MsgTest;
import com.example.detectradiativeresource.dao.DataMsg;
import com.example.detectradiativeresource.monitor.trace.LocationService;
import com.example.detectradiativeresource.route.BNaviMainActivity;
import com.example.detectradiativeresource.utils.BluetoothProtocol;
import com.example.detectradiativeresource.utils.DBScanUtils;
import com.example.detectradiativeresource.utils.DataHelperUtils;
import com.example.detectradiativeresource.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorFragment extends Fragment{
    private static MonitorFragment monitorFragment;
    private boolean isStop = true;
    private static final String TAG = "TraceFragment";
    private Timer timer = null;
    private TimerTask task = null;
    private Timer sendTimer = null;
    private TimerTask sendTask = null;
    private TextView r_valView;
    private TextView n_valView;
    private Button start;
    private Button cal;
    private Button btn_alert;
    private Button btn_navi;
    //private Button btn_spectrum;
    private Vibrator vibrator;
    private MediaPlayer mMediaPlayer;
    private Overlay mAlertOverlay;
    private Overlay mAlertOverlayMsg;
    private ProgressBar progressBar_r;
    private ProgressBar progressBar_n;
    private TextView r_valViewMsg;
    private TextView n_valViewMsg;

    public double longitude=0.0;
    public double latitude=0.0;
    private MapView mMapView = null;    //地图视图
    private BaiduMap mBaiduMap = null;  //地图
    private Overlay last_track = null;  //路径
    private static int ColorStatus; //当前所选用的颜色
    private LocationListener mCallback;
    private int flag=0;//0代表没有找到起始点状态，1代表目前正在朝着指定的方向前进，2代表回到初始点，3代表找到了辐射最大比率方向
    private int now_dir=2;//代表MainActivity中的方向一维坐标方向
    private boolean isColored=false;//当前地图方向line是否绘制
    private int startValue=0;//初始点辐射值
    private double maxValueIncr=0.0;//最大辐射比率
    private int right_dir=-1;//正确方向
    //private boolean setRegion=false;//是否需要设置区域
    private boolean setGuide=false;//是否找到辐射值最强的方向
    private MsgTest test=new MsgTest();
    LocationService locationService;
    LatLng lastPnt = null;
    private View rootView;
    private boolean isAlert=false;//当前位置是否超过报警值
    private String measureVal_r;
    private String measureVal_n;
    private boolean wifiFlag;//蓝牙是否连接
    private boolean isSendAlert=false;//是否发送过报警事件
    private boolean isSendAutoAlertByPlace=false;//区域报警标志
    private MainActivity mainActivity;
    //private boolean isAlertOpen = true; //是否是打开报警状态

    private int sendFlag;//表示目前所处状态，1是连接中，2是已经连接，3是断开。
    //private boolean isNomalOnce=true;//是否是第一次进入正常区域

    /**************************************导航算法部分*************************************/
    private Overlay directionCircle = null;  //路径
    private Overlay directionLine = null;
    private Overlay regionCircle = null;  //路径

    public MonitorFragment(){

    }
    public static MonitorFragment getNewInstance(){
        if (monitorFragment ==null){
            monitorFragment =new MonitorFragment();
        }
        return monitorFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_monitor, container, false);
        initMap(rootView);
        initDetect(rootView);
        mainActivity=(MainActivity)getActivity();
        return rootView;
    }
    private void initMap(View view){
        Activity activity = getActivity();
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(21);

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            //marker被点击时回调的方法
            //若响应点击事件，返回true，否则返回false
            //默认返回false
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle bundle = marker.getExtraInfo();
                String msg=bundle.getString("msg");
                Toast.makeText(getActivity().getApplicationContext(), "辐射值为"+msg, Toast.LENGTH_LONG).show();
                return false;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if(MainActivity.autoFlag&&MainActivity.receivedState!=BluetoothProtocol.NO_STATE){
            MainActivity.autoFlag=false;
            //MainActivity.send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
            startMeasure();
        }
        /*if(MainActivity.isAlertOpen){
            btn_alert.setText("关闭报警");
        }
        else {
            btn_alert.setText("打开报警");
        }*/
        /*if(MainActivity.isSendCloseAlert){
            MainActivity.isSendCloseAlert=false;
        }*/
    }
    @Override
    public void onStart() {
        super.onStart();
        locationService = MainActivity.locationService;
        locationService.registerListener(mListener); //注册监听
        int type = getActivity().getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.setLocationOption(locationService.getOption());
        }
        locationService.start();

    }

    @Override
    public void onPause(){
        super.onPause();
        /*if(MainActivity.receivedState!=BluetoothProtocol.NO_STATE){
            Message message4 = new Message();
            message4.what = 3;
            handler.sendMessage(message4);
        }*/
        myStop();
    }
    @Override
    public void onStop() {
        /*if(MainActivity.isSendCloseAlert){
            MainActivity.isSendCloseAlert=false;
        }*/
        super.onStop();
    }
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                if (location.getLocType() == BDLocation.TypeServerError) {
                    //Toast.makeText(getActivity().getApplicationContext(), "服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    //Toast.makeText(getActivity().getApplicationContext(), "网络不同导致定位失败，请检查网络是否通畅", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    //Toast.makeText(getActivity().getApplicationContext(), "无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机", Toast.LENGTH_LONG).show();
                }
                else{
                    MyLocationData locData = new MyLocationData.Builder()
                            .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                            .direction(100).latitude(location.getLatitude())
                            .longitude(location.getLongitude()).build();
                    //获取经纬度
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    MainActivity.latitude=latitude;
                    MainActivity.longitude=longitude;
                    //Log.i(TAG, "onReceiveLocation: "+"纬度："+Double.toString(latitude)+"；经度：" + Double.toString(longitude) + "；精准度"+ location.getRadius());
                    mBaiduMap.setMyLocationData(locData);
                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    /** 根据经纬度生成一个坐标点
                     * 判断如果是第一次定位则不发生连线
                     * 以后每次将本次定位点与上一次定位点相连接，产生一条轨迹
                     * 根据颜色状态，改变当前轨迹颜色
                     * 如果定位失败，告知用户
                     * */
                    LatLng pnt = new LatLng(location.getLatitude(), location.getLongitude());
                    if(lastPnt == null){
                        lastPnt = pnt;
                        return;
                    }
                    List<LatLng> pointList = new ArrayList<>();
                    pointList.add(lastPnt);
                    pointList.add(pnt);
                    lastPnt = pnt;
                    if(ColorStatus == 0) {
                        PolylineOptions polyline = new PolylineOptions().width(10).color(Color.GRAY).points(pointList);
                        Overlay track = mBaiduMap.addOverlay(polyline);
                    }else if(ColorStatus == 1){
                        PolylineOptions polyline = new PolylineOptions().width(10).color(Color.GREEN).points(pointList);
                        Overlay track = mBaiduMap.addOverlay(polyline);
                    }else if(ColorStatus == 2) {
                        PolylineOptions polyline = new PolylineOptions().width(10).color(Color.RED).points(pointList);
                        Overlay track = mBaiduMap.addOverlay(polyline);
                    }else if(ColorStatus == 3) {
                        PolylineOptions polyline = new PolylineOptions().width(10).color(Color.MAGENTA).points(pointList);
                        Overlay track = mBaiduMap.addOverlay(polyline);
                    }else if(ColorStatus == 4) {
                        PolylineOptions polyline = new PolylineOptions().width(10).color(Color.YELLOW).points(pointList);
                        Overlay track = mBaiduMap.addOverlay(polyline);
                    }else {

                    }
                    if(MainActivity.isNaviStart){
                        findDirection();
                    }
                    if(MainActivity.isNaviEnd){
                        alongWithDirection();
                    }
                    canStartPrediction();
                    //mCallback.setLocation(latitude,longitude);
                    /*setMarker();
                    if(!MainActivity.setRegion){
                        if(flag==0){
                            //Log.i(TAG, "寻找起点");
                            findStart();
                        }
                        if(flag==1||flag==2){
                            //Log.i(TAG, "开始导航");
                            findDirection();
                        }
                        if(flag==3){
                            //Log.i(TAG, "沿着方向");
                            alongWithDirection();
                        }
                    }
                    if(MainActivity.setRegion){
                        double[] now={MainActivity.rightLatitude,MainActivity.rightLongitude};
                        createRegion(now,MainActivity.rightDir);
                    }*/
                }

            }
        }
    };


    /**
     * @description: 初始化和背包有关的组件
     * @author: lyj
     * @create: 2019/10/14
     **/
    private void initDetect(View view){
        ColorStatus = 0;
        r_valView=view.findViewById(R.id.id_r_val);
        n_valView=view.findViewById(R.id.id_n_val);
        cal=view.findViewById(R.id.calculate);
        start = (Button) view.findViewById(R.id.start);
        //start.setBackgroundColor(Color.GREEN);
        btn_alert=view.findViewById(R.id.alert_close);
        btn_navi=view.findViewById(R.id.get_navi);
        //btn_spectrum=view.findViewById(R.id.get_spectrum);
        progressBar_r = (ProgressBar)view.findViewById(R.id.progress_r);
        progressBar_r.setProgress(0);
        progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
        progressBar_n = (ProgressBar)view.findViewById(R.id.progress_n);
        progressBar_n.setProgress(0);
        progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
        r_valViewMsg=view.findViewById(R.id.r_view_msg);
        n_valViewMsg=view.findViewById(R.id.n_view_msg);
        r_valViewMsg.setText("正常");
        n_valViewMsg.setText("正常");
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if(isStop) {
                        //MainActivity.send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
                        /*if(MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS_FAILED||MainActivity.receivedState==BluetoothProtocol.NO_STATE||
                                MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS){
                            MainActivity.send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
                        }
                        else{
                            startMeasure();
                        }*/
                        //Toast.makeText(getActivity().getApplicationContext(), "开始1", Toast.LENGTH_LONG).show();
                        startMeasure();
                    }else{
                        //stopTimer();
                        myStop();
                        //MainActivity.send(BluetoothProtocol.STOP_MEASURE,new byte[]{});
                        /*timer = new Timer();
                        task = new TimerTask() {
                            @Override
                            public void run() {
                                // 需要做的事:发送消息
                                Message message = new Message();
                                message.what = 3;
                                handler.sendMessage(message);
                            }
                        };
                        timer.schedule(task, 500);*/
                        //stopTimer();
                        //start.setText("开始");
                    }

                }catch (Exception e) {
                    Log.i(TAG, "onClick: " + e.toString());
                }
            }
        });
        btn_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(MainActivity.isPredictStart||MainActivity.isEnterRegion){
                    Toast.makeText(getActivity().getApplicationContext(), "预测中或者已经到达预测点位置，导航不可用", Toast.LENGTH_LONG).show();
                    return;
                }*/
                if(MainActivity.isPredictStart){
                    Toast.makeText(getActivity().getApplicationContext(), "预测中或者已经到达预测点位置，导航不可用", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!MainActivity.isNaviStart){
                    Toast.makeText(getActivity().getApplicationContext(), "请在该区域内采集数据，再次点击导航按钮，即可导航", Toast.LENGTH_LONG).show();
                    startDirection();
                }
                else{
                    if(MainActivity.incrByValue!=0){
                        Toast.makeText(getActivity().getApplicationContext(), "请沿当前方向前进", Toast.LENGTH_LONG).show();
                        endDirection();
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(), "采集的点不充分，无法导航，请继续采集一部分数据", Toast.LENGTH_LONG).show();
                    }

                    /*MainActivity.incrByLatitude=0.001;
                    MainActivity.incrByLongitude=0.001;
                    endDirection();*/
                }
            }
        });
        cal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*double[] ans= DBScanUtils.getTestMsg();
                Toast.makeText(getActivity().getApplicationContext(), "预测坐标为"+ans[0]+":"+ans[1], Toast.LENGTH_LONG).show();*/
                if(MainActivity.isEnterRegion){
                    if(!MainActivity.isPredictStart){
                        startPrediction();
                    }
                    else{
                        if(canEndPrediction()){
                            endPrediction();
                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(), "采集数据点不够，请继续采集", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "未到达可以预测区域，请继续按照导航方向前进", Toast.LENGTH_LONG).show();
                }
            }
        });
        btn_alert.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                stopMusic();
                stopViberate();
                /*if(MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS_FAILED||MainActivity.receivedState==BluetoothProtocol.NO_STATE||
                        MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS){
                    MainActivity.send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
                    isSendAlert=true;
                }*/
                if(MainActivity.isAlertOpen){
                    Log.i(TAG, "-------------------主动关闭---------------------------");
                    if(isStop){
                        mainActivity.stopTimer();
                        mainActivity.startTimer(2);
                    }
                    else{
                        isSendAlert=true;
                    }
                    /*MainActivity.stopTimer();
                    MainActivity.startTimer(2);*/
                    //startSendTimer(0);
                    //MainActivity.send(BluetoothProtocol.ALERT_CLOSE,new byte[]{});
                }
                /*else{
                    startSendTimer(1);
                    //MainActivity.send(BluetoothProtocol.ALERT_OPEN,new byte[]{});
                }*/
                MainActivity.isAlertOpen=false;
                /*if(MainActivity.isAlertOpen){
                    *//*if(MainActivity.alertState==BluetoothProtocol.ALERT_START){
                        stopMusic();
                        stopViberate();
                        if(mAlertOverlay!=null){
                            mAlertOverlay.remove();
                        }
                    }*//*
                    Log.i(TAG, "-------------------主动关闭---------------------------");
                    MainActivity.send(BluetoothProtocol.ALERT_CLOSE,new byte[]{});
                }
                else{
                    MainActivity.send(BluetoothProtocol.ALERT_OPEN,new byte[]{});
                }*/
            }
        });
        /*btn_spectrum.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(MainActivity.receivedState==BluetoothProtocol.NO_STATE||MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS_FAILED){
                    Toast.makeText(getActivity().getApplicationContext(), "未连接至背包，请点击开始按钮", Toast.LENGTH_LONG).show();
                }
                else if(MainActivity.receivedState==BluetoothProtocol.START_MEASURE||MainActivity.receivedState==BluetoothProtocol.START_MEASURE_FAILED){
                    Toast.makeText(getActivity().getApplicationContext(), "未开启测量，请点击开始按钮", Toast.LENGTH_LONG).show();
                }
                else{
                    stopTimer();
                    timer = new Timer();
                    task = new TimerTask() {
                        @Override
                        public void run() {
                            // 需要做的事:发送消息
                            Message message = new Message();
                            message.what = 4;
                            handler.sendMessage(message);
                        }
                    };
                    timer.schedule(task, 500);
                }
            }
        });*/
        //setTestMsg();
        //initHistoryPoint();
    }

    private void startMusic(){
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if(mMediaPlayer==null){
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(getContext(), alert);
                //final AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic(){
        try {
            if(this.mMediaPlayer != null) {
                this.mMediaPlayer.stop();
                Log.d(TAG, "---------stop music-----------");
            }
            mMediaPlayer.release();
            mMediaPlayer=null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启震动
     */
    private void startVibrate() {
        /*if (vibrator == null) {
            //获取震动服务
            vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }
        //震动模式隔1秒震动1.4秒
        long[] pattern = { 1000, 1400 };
        //震动重复，从数组的0开始（-1表示不重复）
        vibrator.vibrate(pattern, 0);*/
        //Log.i(TAG, "-------------------震动------------");
        vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 200, 100, 200}; // 100ms后开始震动200ms，然后再停止100ms，再震动200ms
        vibrator.vibrate(pattern, 0);
    }

    /**
     * 停止震动
     */
    private void stopViberate() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
    /**
     * @description: 启动计时器，发送数据
     * @author: lyj
     * @create: 2019/10/14
     **//*
    private void startTimer() {
        if(timer == null) {
            timer = new Timer();
        }
        task=null;
        task = new TimerTask() {
            @Override
            public void run() {
                // 需要做的事:发送消息
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 0,1000);
        Log.d(TAG, "---------start timer-----------");
    }

    *//**
     * @description: 关闭计时器
     * @author: lyj
     * @create: 2019/10/14
     **//*
    private void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        if(task != null) {
            task.cancel();
            task = null;
        }
    }*/

    /**
     * @description: 启动计时器，发送数据
     * @author: lyj
     * @create: 2019/12/13
     **/
    /*private void startSendTimer(int msg) {
        if(sendTimer == null) {
            sendTimer = new Timer();
        }
        sendTask=null;
        switch (msg){
            case 0: //打开报警
                sendTask = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 5;
                        handler.sendMessage(message);
                    }
                };
                sendTimer.schedule(sendTask, 0,1000);
                break;
            case 1:
                sendTask = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 6;
                        handler.sendMessage(message);
                    }
                };
                sendTimer.schedule(sendTask, 0,1000);
                break;

        }
    }

    *//**
     * @description: 关闭计时器
     * @author: lyj
     * @create: 2019/12/13
     **//*
    private void stopSendTimer() {
        if(sendTimer != null) {
            sendTimer.cancel();
            sendTimer = null;
        }

        if(sendTask != null) {
            sendTask.cancel();
            sendTask = null;
        }
    }

    *//**
     * @description: handler,蓝牙协议核心
     * @author: lyj
     * @create: 2019/11/12
     **//*
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    MainActivity.send(BluetoothProtocol.GET_DATA,new byte[]{});
                    break;
                case 2:
                    break;
                case 3:
                    MainActivity.send(BluetoothProtocol.STOP_MEASURE,new byte[]{});
                    break;
                case 4:
                    MainActivity.send(BluetoothProtocol.GET_SPECTRUM,new byte[]{});
                    break;
                case 5:
                    MainActivity.send(BluetoothProtocol.ALERT_CLOSE,new byte[]{});
                    break;
                case 6:
                    MainActivity.send(BluetoothProtocol.ALERT_OPEN,new byte[]{});
                    break;
            }
            super.handleMessage(msg);
        }
    };*/

    //开启测量
    public void startMeasure(){
        start.setText("停止");
        isStop = false;
        if(DataHelperUtils.isNewDate()){
            DataHelperUtils.saveDataTotalMsg();
        }
        mainActivity.stopTimer();
        mainActivity.startTimer(3);
        //Toast.makeText(getActivity().getApplicationContext(), "开始2", Toast.LENGTH_LONG).show();
    }

    /**
     * @description: 处理接受数据
     * @author: lyj
     * @create: 2019/11/26
     **/
    public void handlerReceivedData(String Type,int[] data){
        switch (Type){
            /*case BluetoothProtocol.SHAKE_HANDS_OK:
                //MainActivity.send(BluetoothProtocol.START_MEASURE,new byte[]{});
                *//*start.setText("停止");
                //start.setBackgroundColor(Color.RED);
                isStop = !isStop;
                DataHelperUtils.saveDataTotalMsg();
                startTimer();*//*
                if(isSendAlert){
                    if(MainActivity.isAlertOpen){
                        Log.i(TAG, "-------------------主动关闭---------------------------");
                        startSendTimer(0);
                    }
                    else{
                        startSendTimer(1);
                    }
                    isSendAlert=false;
                }
                else{
                    startMeasure();
                }
                //startMeasure();
                break;
            case BluetoothProtocol.SHAKE_HANDS_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "握手失败", Toast.LENGTH_LONG).show();
                break;*/
            /*case BluetoothProtocol.START_MEASURE_OK:
                start.setText("停止");
                start.setBackgroundColor(Color.RED);
                isStop = !isStop;
                DataHelperUtils.saveDataTotalMsg();
                startTimer();
                break;
            case BluetoothProtocol.START_MEASURE_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "启动测量失败", Toast.LENGTH_LONG).show();
                break;*/
            case BluetoothProtocol.GET_DATA:
                //DataHelperUtils.saveDataTotalMsg();
                //报警转正常自动关闭报警
                if(MainActivity.alertState==BluetoothProtocol.ALERT_START&&((MainActivity.valType==1&&data[23]==0)||(MainActivity.valType==2&&data[25]==0))){
                    MainActivity.isAlertOpen=false;
                    Log.i(TAG, "-------------------正常区域关报警------------");
                    //isSendAlert=true;
                    isSendAlert=false;
                    isSendAutoAlertByPlace=true;
                }
                if(isSendAutoAlertByPlace){
                    isSendAutoAlertByPlace=false;
                    mainActivity.stopTimer();
                    mainActivity.startTimer(4);
                }
                if(isSendAlert){
                    isSendAlert=false;
                    mainActivity.stopTimer();
                    mainActivity.startTimer(2);
                }
                UpdateUI(data);
                break;
            case BluetoothProtocol.STOP_MEASURE_OK:
                MainActivity.receivedState=BluetoothProtocol.NO_STATE;
                myStop();
                break;
            case BluetoothProtocol.STOP_MEASURE_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "停止测量失败", Toast.LENGTH_LONG).show();
                //startTimer();
                break;
            case BluetoothProtocol.ALERT_START:
                setAlert(data);
                break;
            case BluetoothProtocol.ALERT_CLOSE_OK:
                Toast.makeText(getActivity().getApplicationContext(), "关闭报警成功", Toast.LENGTH_LONG).show();
                MainActivity.alertState=BluetoothProtocol.NO_STATE;
                mainActivity.stopTimer();
                stopMusic();
                stopViberate();
                /*if(mAlertOverlay!=null){
                    mAlertOverlay.remove();
                }*/
                if(!isStop){
                    startMeasure();
                }
                /*stopSendTimer();
                MainActivity.alertState=BluetoothProtocol.NO_STATE;
                if(MainActivity.isAlertOpen){
                    MainActivity.isAlertOpen=false;
                    stopMusic();
                    stopViberate();
                    if(mAlertOverlay!=null){
                        mAlertOverlay.remove();
                    }
                }
                //MainActivity.isAlertOpen=!MainActivity.isAlertOpen;
                btn_alert.setText("打开报警");*/
                break;
            case BluetoothProtocol.ALERT_CLOSE_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "关闭报警失败，请重新关闭", Toast.LENGTH_LONG).show();
                MainActivity.alertState=BluetoothProtocol.ALERT_START;
                break;
            /*case BluetoothProtocol.ALERT_OPEN_OK:
                stopSendTimer();
                Toast.makeText(getActivity().getApplicationContext(), "打开报警成功", Toast.LENGTH_LONG).show();
                MainActivity.alertState=BluetoothProtocol.NO_STATE;
                MainActivity.isAlertOpen=!MainActivity.isAlertOpen;
                btn_alert.setText("关闭报警");
                break;
            case BluetoothProtocol.ALERT_OPEN_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "打开报警失败，请重新打开", Toast.LENGTH_LONG).show();
                MainActivity.alertState=BluetoothProtocol.ALERT_OPEN;
                break;*/
            /*case BluetoothProtocol.GET_SPECTRUM:
                MainActivity.getSpectrum=BluetoothProtocol.NO_STATE;
                handlerSpectrumData(data);
                break;*/
        }

    }

    private void setAlert(int[] data) {
        //isAlert=true;
        if(!MainActivity.isAlertOpen){
            return;
        }
        if(MainActivity.isAlertMusic){
            startMusic();
        }
        startVibrate();
    }

    /**
     * @description: 更新页面
     * @author: lyj
     * @create: 2019/10/14
     **/
    public void UpdateUI(int[] data){
        //在地图上显示文字覆盖物
        if(mAlertOverlayMsg!=null){
            mAlertOverlayMsg.remove();
        }
        /*if(mAlertOverlay!=null){
            mAlertOverlay.remove();
        }
        String text="";
        int[] type=BluetoothProtocol.getTypeArrayByTwo(BluetoothProtocol.getVal(data,18,19));
        if(type[0]==1){
            text+="Cs137,";
        }
        if(type[1]==1){
            text+="U235,";
        }
        if(type[2]==1){
            text+="U238,";
        }
        if(type[3]==1){
            text+="Th232,";
        }
        if(type[4]==1){
            text+="K40,";
        }
        if(type[5]==1){
            text+="I131,";
        }
        if(type[6]==1){
            text+="Ba133,";
        }
        if(type[7]==1){
            text+="Co57,";
        }
        if(type[8]==1){
            text+="Co60,";
        }
        if(type[9]==1){
            text+="Tc99m,";
        }
        if(type[10]==1){
            text+="Am241,";
        }
        *//*if(type[11]==1){
            text+="Ba133,";
        }
        if(type[12]==1){
            text+="Cs137,";
        }
        if(type[13]==1){
            text+="Co57,";
        }
        if(type[14]==1){
            text+="Co60,";
        }
        if(type[15]==1){
            text+="Am241,";
        }*//*
        if(!text.equals("")){
            //Log.i(TAG, "长度是！！！！！！: " + text.length());
            text=text.substring(0,text.length()-1);
            LatLng llText = new LatLng(MainActivity.latitude+0.00005, MainActivity.longitude);
            OverlayOptions mTextOptions = new TextOptions()
                    .text(text) //文字内容
                    .bgColor(0xAAFFFF00) //背景色
                    .fontSize(48) //字号
                    .fontColor(0xFFFF00FF) //文字颜色
                    .rotate(0) //旋转角度
                    .position(llText);
            mAlertOverlay = mBaiduMap.addOverlay(mTextOptions);
        }*/
        String msg="";
        switch (data[1]){
            case 0:
                msg="";
                break;
            case 1:
                msg="通讯故障";
                //progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                break;
            case 2:
                msg="稳谱故障";
                break;
            case 3:
                msg="高压故障";
                //progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                break;
            case 4:
                msg="NaI故障";
                break;
            case 5:
                msg="GM管故障";
                //progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                break;
            case 6:
                msg="中子探测器故障";
                break;
            case 7:
                msg="高本底故障";
                //progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                break;
            case 8:
                msg="低本底故障";
                break;

        }

        if(!msg.equals("")){
            LatLng llText1 = new LatLng(MainActivity.latitude-0.00005, MainActivity.longitude);
            OverlayOptions mTextOptions1 = new TextOptions()
                    .text(msg) //文字内容
                    .bgColor(0xAAFFFF00) //背景色
                    .fontSize(48) //字号
                    .fontColor(0xFFFF00FF) //文字颜色
                    .rotate(0) //旋转角度
                    .position(llText1);
            mAlertOverlayMsg = mBaiduMap.addOverlay(mTextOptions1);
        }
        MainActivity.Nai_jishu=BluetoothProtocol.getVal(data,2,3);
        MainActivity.Nai_jiliang=BluetoothProtocol.getVal(data,4,7);
        MainActivity.GM_jishu=BluetoothProtocol.getVal(data,8,9);
        MainActivity.GM_jiliang=BluetoothProtocol.getVal(data,10,13);
        MainActivity.n_jishu=BluetoothProtocol.getVal(data,14,15);
        MainActivity.n_jiliang=BluetoothProtocol.getVal(data,16,17);
        boolean isAlert=false;
        if(data[23]!=0){
            isAlert=true;
        }
        if(MainActivity.isPredictStart){
            DataHelperUtils.saveDataMsg(String.valueOf(MainActivity.Nai_jishu),String.valueOf(MainActivity.Nai_jiliang),String.valueOf(MainActivity.GM_jishu),
                    String.valueOf(MainActivity.GM_jiliang),String.valueOf(MainActivity.n_jishu),String.valueOf(MainActivity.n_jiliang),longitude,latitude,2,isAlert);
        }
        else{
            DataHelperUtils.saveDataMsg(String.valueOf(MainActivity.Nai_jishu),String.valueOf(MainActivity.Nai_jiliang),String.valueOf(MainActivity.GM_jishu),
                    String.valueOf(MainActivity.GM_jiliang),String.valueOf(MainActivity.n_jishu),String.valueOf(MainActivity.n_jiliang),longitude,latitude,0,isAlert);
        }
        Log.i(TAG, "---------save-------------------is "+isAlert);
        /*switch (data[24]){
            case 0:
                n_valViewMsg.setText("正常");
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_normal));
                break;
            case 1:
                n_valViewMsg.setText("轻微报警");
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                DataHelperUtils.saveLogMsg("报警","轻微报警");
                break;
            case 2:
                n_valViewMsg.setText("中度报警");
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                DataHelperUtils.saveLogMsg("报警","中度报警");
                break;
            case 3:
                n_valViewMsg.setText("严重报警");
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                DataHelperUtils.saveLogMsg("报警","严重报警");
                break;
        }*/
        if(MainActivity.valType==1){
            switch (data[23]){
                case 0:
                    r_valViewMsg.setText("正常");
                    progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_normal));
                    break;
                case 1:
                    r_valViewMsg.setText("轻微报警");
                    progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                    DataHelperUtils.saveLogMsg("报警","轻微报警");
                    break;
                case 2:
                    r_valViewMsg.setText("中度报警");
                    progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                    DataHelperUtils.saveLogMsg("报警","中度报警");
                    break;
                case 3:
                    r_valViewMsg.setText("严重报警");
                    progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                    DataHelperUtils.saveLogMsg("报警","严重报警");
                    break;
            }
            int r_val=BluetoothProtocol.getVal(data,2,3);
            int n_val=BluetoothProtocol.getVal(data,14,15);
            measureVal_r=String.valueOf(r_val)+" CPS";
            measureVal_n=String.valueOf(n_val);
            r_valView.setText(measureVal_r);
            n_valView.setText(measureVal_n);
            if(r_val==0){
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
                //MainActivity.isAlertOpen=true;
                //MainActivity.isSendCloseAlert=false;

            }
            if(n_val==0){
                progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
            }
            /*if(r_val>MainActivity.alert_r_jishu){
                //progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                //r_valViewMsg.setText("报警");
                DataHelperUtils.saveLogMsg("报警","警告时间");
                //isNomalOnce=true;
            }
            else if(r_val==0){
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
                //MainActivity.isAlertOpen=true;
                //MainActivity.isSendCloseAlert=false;

            }
            else{
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_normal));
                *//*MainActivity.isAlertOpen=true;
                if(isNomalOnce){
                    MainActivity.isSendCloseAlert=false;
                    isNomalOnce=false;
                }*//*
            }*/
            /*if(n_val>MainActivity.alert_n_jishu){
                //progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                //n_valViewMsg.setText("报警");
                DataHelperUtils.saveLogMsg("报警","警告时间");
                //isAlert=true;
            }
            else if(n_val==0){
                progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
            }
            else{
                progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_normal));
            }*/
            progressBar_r.setProgress((int) ((r_val+0.1)/MainActivity.total_r_jishu*80+20));
            progressBar_n.setProgress((int) ((n_val+0.1)/MainActivity.total_n_jishu*80+20));
            if(isAlert){
                //DataHelperUtils.updateDataTotalMsgIsAlarm();
            }
        }
        else if(MainActivity.valType==2){
            switch (data[25]){
                case 0:
                    r_valViewMsg.setText("正常");
                    progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_normal));
                    break;
                case 1:
                    r_valViewMsg.setText("轻微报警");
                    progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                    DataHelperUtils.saveLogMsg("报警","轻微报警");
                    break;
                case 2:
                    r_valViewMsg.setText("中度报警");
                    progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                    DataHelperUtils.saveLogMsg("报警","中度报警");
                    break;
                case 3:
                    r_valViewMsg.setText("严重报警");
                    progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                    DataHelperUtils.saveLogMsg("报警","严重报警");
                    break;
            }
            int r_val=BluetoothProtocol.getVal(data,4,7);
            double m=100;
            double r_val_d=(double)r_val/m;
            int n_val=BluetoothProtocol.getVal(data,14,15);
            if(r_val_d<1000){
                measureVal_r=String.valueOf(r_val_d)+" uSv/h";
            }
            else if(r_val_d<1000000){
                double n=1000;
                measureVal_r=String.valueOf(r_val_d/n)+" mSv/h";
            }
            else{
                double n=1000;
                measureVal_r=String.valueOf(r_val_d/n)+" Sv/h";
            }
            measureVal_n=String.valueOf(n_val);
            r_valView.setText(measureVal_r);
            n_valView.setText(measureVal_n);
            if(r_val==0){
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
                //MainActivity.isAlertOpen=true;
                //MainActivity.isSendCloseAlert=false;

            }
            if(n_val==0){
                progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
            }
            /*if(r_val>MainActivity.alert_r_jiliang){
                //progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                //r_valViewMsg.setText("报警");
                DataHelperUtils.saveLogMsg("报警","警告时间");
                //isAlert=true;
                //isNomalOnce=true;
            }
            else if(r_val==0){
                //MainActivity.isAlertOpen=true;
                //MainActivity.isSendCloseAlert=false;
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
            }
            else{
                progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_normal));
               *//* MainActivity.isAlertOpen=true;
                if(isNomalOnce){
                    MainActivity.isSendCloseAlert=false;
                    isNomalOnce=false;
                }*//*
            }
            if(n_val>MainActivity.alert_n_jiliang){
                //progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_alert));
                //n_valViewMsg.setText("报警");
                DataHelperUtils.saveLogMsg("报警","警告时间");
                //isAlert=true;
            }
            else if(n_val==0){
                progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
            }
            else{
                progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_normal));
            }*/
            progressBar_r.setProgress((int) ((r_val+0.1)/MainActivity.total_r_jiliang*80+20));
            progressBar_n.setProgress((int) ((n_val+0.1)/MainActivity.total_n_jiliang*80+20));
            if(isAlert){
                //DataHelperUtils.updateDataTotalMsgIsAlarm();
            }
        }
    }

    /**
     * @description: 停止测量时修改的一些状态位
     * @author: lyj
     * @create: 2019/10/14
     **/
    public void myStop(){
        start.setText("开始");
        //start.setBackgroundColor(Color.GREEN);
        isStop=true;
        r_valView.setText("0");
        r_valView.setBackgroundColor(Color.WHITE);
        n_valView.setText("0");
        n_valView.setBackgroundColor(Color.WHITE);
        progressBar_r.setProgress(0);
        progressBar_r.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
        r_valViewMsg.setText("正常");
        progressBar_n.setProgress(0);
        progressBar_n.setProgressDrawable(getResources().getDrawable(R.drawable.layer_progress_init));
        n_valViewMsg.setText("正常");
        mainActivity.stopTimer();
        DataHelperUtils.dataTotalMsg_IsAlarm_Now=false;
        DataHelperUtils.updateDataTotalMsgTime();
    }

    /**
     * @description:获取辐射值
     * @author: lyj
     * @create: 2019/11/12
     **/
    private int getVal(int[] data,int start,int end){
        int ans=0;
        for(int i=start;i<=end;i++){
            ans+=data[i]*Math.pow(16,2*(i-start));
        }
        Log.i(TAG, "计算的数据是！！！！！！: " + ans);
        return ans;
    }

    /**
     * @description: 更新页面
     * @author: lyj
     * @create: 2019/10/14
     **//*
    private void UpdateUI1(int[] data) {
        for(int d:data){
            Log.i(TAG, "计算的数据内容是！！！！！！: " + d);
        }
        int val=getVal(data,1,4); //1-4位是获取的数值
        measureVal=String.valueOf(val);
        if(val>MainActivity.errorVal){
            ColorStatus = 4;
            r_valView.setBackgroundColor(Color.YELLOW);
        }
        else if(val>MainActivity.seriousVal){
            ColorStatus = 3;
            r_valView.setBackgroundColor(Color.MAGENTA);
            DataHelperUtils.saveLogMsg("报警","报警时间");
        }
        else if(val>MainActivity.alarmVal){
            ColorStatus = 2;
            r_valView.setBackgroundColor(Color.RED);
            DataHelperUtils.saveLogMsg("报警","警告时间");
        }
        else{
            ColorStatus = 1;
            r_valView.setBackgroundColor(Color.GREEN);
        }
        DataHelperUtils.saveDataMsg(measureVal,longitude,latitude,0);
        DataHelperUtils.updateDataTotalMsgIsAlarm();
        r_valView.setText(measureVal);
    }*/

    /**
     * @description: 获取当前时间
     * @author: lyj
     * @create: 2019/09/02
     **/
    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = dateFormat.format(new java.util.Date());
        return date;
    }

    /**************************************历史轨迹点部分*************************************/

    /**
     * @description: 历史轨迹点的测试
     * @author: lyj
     * @create: 2019/09/27
     **/
    public void setMarker(){
        if(measureVal_r==null||measureVal_r.length()==0){
            //Log.i(TAG, "measureVal---------null");
            return;
        }
        if(isRepeatPoint()){
            //Log.i(TAG, "repeated---------null");
            return;
        }
        //Log.i(TAG, "find-----------------------------------------------------------------------go");
        Bundle mBundle = new Bundle();
        mBundle.putString("msg", measureVal_r);
        LatLng point = new LatLng(latitude, longitude);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_end);
        MarkerOptions option = new MarkerOptions().position(point).icon(bitmap).draggable(true).extraInfo(mBundle).flat(true).alpha(0.5f);
        mBaiduMap.addOverlay(option);
        //今晚就改的内容
        //DataHelperUtils.saveDataMsg(measureVal_r,longitude,latitude,1,isAlert);
        /*DataMsg msg=new DataMsg(getTime(),measureVal,longitude,latitude,1);
        msg.save();*/
    }
    /**
     * @description: 绘制数据库轨迹点
     * @author: lyj
     * @create: 2019/09/29
     **/
    public void initHistoryPoint(){
        List<DataMsg> list=DataMsg.find(DataMsg.class,"status = ?","1");
        if(list==null||list.size()==0){
            return;
        }
        for(DataMsg data:list){
            Bundle mBundle = new Bundle();
            mBundle.putString("msg", data.getN_jishu());
            LatLng point = new LatLng(data.getLatitude(), data.getLongitude());
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_end);
            MarkerOptions option = new MarkerOptions().position(point).icon(bitmap).draggable(true).extraInfo(mBundle).flat(true).alpha(0.5f);
            mBaiduMap.addOverlay(option);
        }
    }

    /**
     * @description: 存储测试数据，后期删除
     * @author: lyj
     * @create: 2019/09/29
     **/
    public void setTestMsg(){
        DataMsg msg=new DataMsg(getTime(),String.valueOf(1111),"0","0","0","0","0",116.364534,39.970186,1,"否",DataHelperUtils.dataTotalMsg_Id_Now);
        //msg.save();
    }

    /**
     * @description: 周围范围查询函数，查询是否有点已经录入了
     * @author: lyj
     * @create: 2019/09/29
     **/
    public boolean isRepeatPoint(){
        int Latitude_point=String.valueOf(latitude).indexOf(".");
        int longitude_point=String.valueOf(longitude).indexOf(".");
        Log.i(TAG, String.valueOf(latitude-MainActivity.interval).substring(0,Latitude_point+7)+":"+String.valueOf(latitude+MainActivity.interval).substring(0,Latitude_point+7)+":"+String.valueOf(longitude-MainActivity.interval).substring(0,longitude_point+7)+":"+String.valueOf(longitude+MainActivity.interval).substring(0,longitude_point+7));
        List<DataMsg> list=DataMsg.find(DataMsg.class,"status = ? and latitude > ? and latitude < ? and longitude > ? and longitude < ?",
                "1",String.valueOf(latitude-MainActivity.interval).substring(0,Latitude_point+7),String.valueOf(latitude+MainActivity.interval).substring(0,Latitude_point+7),String.valueOf(longitude-MainActivity.interval).substring(0,longitude_point+7),String.valueOf(longitude+MainActivity.interval).substring(0,longitude_point+7));
        Log.i(TAG,"---------"+String.valueOf(list==null)+String.valueOf(list.size()));
        return list.size()!=0;
    }



    /**************************************导航算法部分*************************************/
    /**
     * @description: 导航算法开启中
     * @author: lyj
     * @create: 2019/12/20
     **/
    public void startDirection(){
        MainActivity.isNaviStart=true;
        MainActivity.isNaviEnd=false;
        MainActivity.startLatitude=MainActivity.latitude;
        MainActivity.startLongitude=MainActivity.longitude;
        MainActivity.startNaIJiShu=MainActivity.Nai_jishu;
        MainActivity.incrByValue=0;
        MainActivity.incrByLatitude=0;
        MainActivity.incrByLongitude=0;
        if(directionLine!=null){
            directionLine.remove();
        }
        if(directionCircle!=null){
            directionCircle.remove();
        }
        //圆心位置
        LatLng center = new LatLng(MainActivity.startLatitude, MainActivity.startLongitude);

        //构造CircleOptions对象
        CircleOptions mCircleOptions = new CircleOptions().center(center)
                .radius(30)
                .fillColor(0xAA0000FF) //填充颜色
                .stroke(new Stroke(1, 0xAA00ff00)); //边框宽和边框颜色

        // 在地图上显示圆
        directionCircle = mBaiduMap.addOverlay(mCircleOptions);
    }

    /**
     * @description: 导航算法进行中
     * @author: lyj
     * @create: 2019/12/20
     **/
    public void findDirection(){
        if(MainActivity.Nai_jishu>MainActivity.startNaIJiShu){
            LatLng start=new LatLng(MainActivity.startLatitude,MainActivity.startLongitude);
            double distance=DistanceUtil.getDistance(new LatLng(MainActivity.latitude,MainActivity.longitude),start);
            if(distance==0){
                return;
            }
            double incr=(MainActivity.Nai_jishu-MainActivity.startNaIJiShu)/distance;
            if(incr>MainActivity.incrByValue){
                MainActivity.incrByValue=incr;
                MainActivity.incrByLatitude=(MainActivity.latitude-MainActivity.startLatitude)*10;
                MainActivity.incrByLongitude=(MainActivity.longitude-MainActivity.startLongitude)*10;
            }
        }
    }

    /**
     * @description: 导航算法完成
     * @author: lyj
     * @create: 2019/12/20
     **/
    public void endDirection(){
        MainActivity.isNaviStart=false;
        MainActivity.isNaviEnd=true;
        if(directionCircle!=null){
            directionCircle.remove();
        }
    }

    /**
     * @description: 导航中
     * @author: lyj
     * @create: 2019/12/20
     **/
    public void alongWithDirection(){
        if(directionLine!=null){
            directionLine.remove();
        }
        LatLng start=new LatLng(MainActivity.latitude,MainActivity.longitude);
        LatLng aim=new LatLng(MainActivity.latitude+MainActivity.incrByLatitude,MainActivity.longitude+MainActivity.incrByLongitude);
        List<LatLng> list = new ArrayList<>();
        list.add(start);
        list.add(aim);
        PolylineOptions polyline = new PolylineOptions().width(10).color(Color.RED).points(list);
        directionLine=mBaiduMap.addOverlay(polyline);
    }

    /**
     * @description: 预测算法是否可以开始
     * @author: lyj
     * @create: 2019/12/20
     **/
    public void canStartPrediction(){
        //判断
        if(MainActivity.Nai_jishu>MainActivity.maxValue){
            MainActivity.isEnterRegion=true;
        }
        //测试阶段使用，记得关闭
        //MainActivity.isEnterRegion=true;
    }


    /**
     * @description: 预测算法开启
     * @author: lyj
     * @create: 2019/12/20
     **/
    public void startPrediction(){
        MainActivity.isNaviEnd=false;

        MainActivity.isPredictStart=true;
        MainActivity.isPredictEnd=false;

        if(directionLine!=null){
            directionLine.remove();
        }
        if(directionCircle!=null){
            directionCircle.remove();
        }
        if(regionCircle!=null){
            regionCircle.remove();
        }
        //圆心位置
        LatLng center = new LatLng(MainActivity.latitude, MainActivity.longitude);

        //构造CircleOptions对象
        CircleOptions mCircleOptions = new CircleOptions().center(center)
                .radius(30)
                .fillColor(0xAA0000FF) //填充颜色
                .stroke(new Stroke(1, 0xAA00ff00)); //边框宽和边框颜色

        // 在地图上显示圆
        regionCircle = mBaiduMap.addOverlay(mCircleOptions);
    }

    /**
     * @description: 预测算法结束
     * @author: lyj
     * @create: 2019/12/20
     **/
    public void endPrediction(){
        MainActivity.isPredictStart=false;
        MainActivity.isPredictEnd=true;
        if(regionCircle!=null){
            regionCircle.remove();
        }
        //预测部分
        ArrayList<ArrayList<Double>> data=DataHelperUtils.findDataByNowDateAsList();
        /*Log.i(TAG, "------------------------list size is : " + data.size());
        for(int i=0;i<data.size();i++){
            Log.i(TAG, "------------------------list is : " + data.get(i).get(0)+" "+data.get(i).get(1)+" "+data.get(i).get(2));
        }*/
        StringBuilder sb=new StringBuilder();
        for(ArrayList<Double>data1:data){
            Log.e("help", "------------------------------data is"+data1.get(0)+" "+data1.get(1)+" "+data1.get(2));
            for(double data2:data1){
                sb.append(data2);
                sb.append(" ");
            }
            sb.append("# ");
        }
        FileUtils.writeTxtToFile(sb.toString(), "/sdcard/Predict/", FileUtils.getFileName());

        double[] ans=DBScanUtils.DBSCAN(data);
        if(ans[0]==0.0){
            Toast.makeText(getActivity().getApplicationContext(), "数据采集不足，无法预测", Toast.LENGTH_LONG).show();
        }
        else{
            //theWayToPredictionPoint(ans);
            LatLng point = new LatLng(ans[0], ans[1]);
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_end);
            MarkerOptions option = new MarkerOptions().position(point).icon(bitmap).draggable(true).flat(true).alpha(0.5f);
            mBaiduMap.addOverlay(option);
            Toast.makeText(getActivity().getApplicationContext(), "预测坐标点为： "+ans[0]+" : "+ans[1], Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(getActivity().getApplicationContext(), "size is"+data.size(), Toast.LENGTH_LONG).show();
    }

    /**
     * @description: 预测算法是否可以结束
     * @author: lyj
     * @create: 2019/12/20
     **/
    public boolean canEndPrediction(){
        if(DataHelperUtils.findDataByNowDateAsList().size()<MainActivity.predictSize){
            return false;
        }
        return true;
    }

    /**
     * @description: 预测点导航
     * @author: lyj
     * @create: 2019/12/25
     **/
    public void theWayToPredictionPoint(double[] data){
        Intent intent = new Intent(getActivity(), BNaviMainActivity.class);
        Log.i(TAG, "------------------------end_longitude is : " + String.valueOf(data[0]));
        Log.i(TAG, "------------------------end_latitude is : " + String.valueOf(data[1]));
        intent.putExtra("end_longitude",String.valueOf(data[0]));
        intent.putExtra("end_latitude",String.valueOf(data[1]));
        intent.putExtra("start_longitude", MainActivity.longitude);
        intent.putExtra("start_latitude", MainActivity.latitude);
        startActivity(intent);
    }







    /**
     * @description: 绘制初始点
     * @author: lyj
     * @create: 2019/09/10
     **//*
    public void pointStart() {
        mBaiduMap.clear();
        //创建测试的起点坐标
        LatLng point = new LatLng(MainActivity.startLatitude,MainActivity.startLongitude);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_start);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }

    *//**
     * @description: 开启引路算法
     * @author: lyj
     * @create: 2019/09/10
     **//*
    public void findStart() {
        *//*if(MainActivity.testFlag){
            startValue=test.findData();
            test.find();
            if(startValue!=0){
                Toast.makeText(getActivity().getApplicationContext(), String.valueOf(startValue), Toast.LENGTH_LONG).show();
                MainActivity.startLongitude=longitude;
                MainActivity.startLatitude=latitude;
                flag=1;
                pointStart();
            }
        }*//*
        if(MainActivity.openNavi){
            startValue=MainActivity.nowValue;
            if(startValue!=0){
                Toast.makeText(getActivity().getApplicationContext(), String.valueOf(startValue), Toast.LENGTH_LONG).show();
                MainActivity.startLongitude=longitude;
                MainActivity.startLatitude=latitude;
                flag=1;
                pointStart();
            }
        }
    }

    *//**
     * @description: 引路算法
     * @author: lyj
     * @create: 2019/09/06
     **//*
    private void findDirection(){
        LatLng start=new LatLng(MainActivity.startLatitude,MainActivity.startLongitude);
        if(flag==1){
            if(now_dir==-1){
                now_dir++;//初始化方向操作
            }
            LatLng aim=new LatLng(MainActivity.startLatitude+MainActivity.directions[now_dir][1],MainActivity.startLongitude+MainActivity.directions[now_dir][0]);
            if(!isColored){//若没有绘制路线，则绘制路线
                List<LatLng> list = new ArrayList<>();
                list.add(start);
                list.add(aim);
                PolylineOptions polyline = new PolylineOptions().width(10).color(Color.BLACK).points(list);
                Overlay track = mBaiduMap.addOverlay(polyline);
                isColored=!isColored;
                return;
            }
            Toast.makeText(getActivity().getApplicationContext(), "请沿着黑色指引路线到达指定地点", Toast.LENGTH_LONG).show();
            *//*double distance1=DistanceUtil.getDistance(new LatLng(MainActivity.Latitude,MainActivity.Longitude),aim);
            Toast.makeText(getActivity().getApplicationContext(), "距离:"+distance1, Toast.LENGTH_LONG).show();*//*
            if(DistanceUtil.getDistance(new LatLng(MainActivity.latitude,MainActivity.longitude),aim)<3.5){
                calculateDir();//计算辐射比率
                flag=2;//切换模式
            }
        }
        else if(flag==2){
            Toast.makeText(getActivity().getApplicationContext(), "已经到达，请沿着黑色指引路线原路返回", Toast.LENGTH_LONG).show();
            if(DistanceUtil.getDistance(new LatLng(MainActivity.latitude,MainActivity.longitude),start)<3.5){
                mBaiduMap.clear();
                pointStart();
                if(now_dir==7){
                    Toast.makeText(getActivity().getApplicationContext(), "完成", Toast.LENGTH_LONG).show();
                    flag=3;
                    now_dir=-1;
                    isColored=!isColored;
                    return;
                }
                now_dir+=1;
                isColored=!isColored;
                flag=1;
                return;
            }
            Toast.makeText(getActivity().getApplicationContext(), "已经到达，请沿着黑色指引路线原路返回", Toast.LENGTH_LONG).show();
        }
    }

    *//**
     * @description: 目前最大辐射比率方向计算
     * @author: lyj
     * @create: 2019/09/10
     **//*
    public void calculateDir() {
        Log.i(TAG, "calculate Dirnb ，flag==1");
        //int nextValue=test.findData();
        int nextValue=MainActivity.nowValue;
        Toast.makeText(getActivity().getApplicationContext(), "当前辐射值"+nextValue, Toast.LENGTH_LONG).show();
        double distance=DistanceUtil.getDistance(new LatLng(MainActivity.latitude,MainActivity.longitude),new LatLng(MainActivity.startLatitude,MainActivity.startLongitude));
        double incr=(nextValue-startValue)/distance;
        if(incr>maxValueIncr){
            right_dir=now_dir;
        }
    }

    *//**
     * @description: 引导沿着辐射比率最大方向
     * @author: lyj
     * @create: 2019/09/10
     **//*
    public void alongWithDirection() {
        if(!setGuide){
            LatLng now=new LatLng(MainActivity.latitude,MainActivity.longitude);
            LatLng next=new LatLng(MainActivity.latitude+MainActivity.directions[right_dir][1]*5,MainActivity.longitude+MainActivity.directions[right_dir][0]*5);
            List<LatLng> list = new ArrayList<>();
            list.add(now);
            list.add(next);
            PolylineOptions polyline = new PolylineOptions().width(10).color(Color.BLACK).points(list);
            Overlay track = mBaiduMap.addOverlay(polyline);
            setGuide=!setGuide;
        }
        //int nowValue=test.findData();//判断此时的辐射值是否减小或者到达最大值
        int nowValue=MainActivity.nowValue;
        Toast.makeText(getActivity().getApplicationContext(), "当前辐射值"+nowValue, Toast.LENGTH_LONG).show();
        if(nowValue>=MainActivity.maxValue){
            MainActivity.rightDir=right_dir;
            MainActivity.rightLatitude=latitude;
            MainActivity.rightLongitude=longitude;
            MainActivity.setRegion=!MainActivity.setRegion;
            return;
        }
        if(nowValue<startValue){
            setGuide=!setGuide;
            flag=1;
            findStart();
            return;
        }
        startValue=nowValue;
    }

    *//**
     * @description: 绘制区域
     * @author: lyj
     * @create: 2019/09/18
     **//*
    public void createRegion(double[] now,int dir) {
        mBaiduMap.clear();
        //setRegion=!setRegion;
        for(int i=0;i<10;i++){
            LatLng mid=new LatLng(now[0]-MainActivity.directions[dir][1]*i,now[1]-MainActivity.directions[dir][0]*i);
            int next_dir;
            if(dir>5){
                next_dir=dir-2;
            }
            else{
                next_dir=dir+2;
            }
            LatLng start=new LatLng(mid.latitude+MainActivity.directions[next_dir][1]*6,mid.longitude+MainActivity.directions[next_dir][0]*6);
            LatLng next=new LatLng(mid.latitude-MainActivity.directions[next_dir][1]*6,mid.longitude-MainActivity.directions[next_dir][0]*6);
            List<LatLng> list = new ArrayList<>();
            list.add(start);
            list.add(next);
            PolylineOptions polyline = new PolylineOptions().width(10).color(Color.BLACK).points(list);
            Overlay track = mBaiduMap.addOverlay(polyline);
        }
        Toast.makeText(getActivity().getApplicationContext(), "请沿着黑色路线依次采集数据", Toast.LENGTH_LONG).show();
    }*/
}


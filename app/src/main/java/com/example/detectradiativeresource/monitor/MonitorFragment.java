package com.example.detectradiativeresource.monitor;

import android.app.Activity;
import android.graphics.Color;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.detectradiativeresource.bluetooth.library.BluetoothSPP;
import com.example.detectradiativeresource.data.DataMsg;
import com.example.detectradiativeresource.monitor.trace.LocationService;

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
    private TextView valView;
    private Button start;

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
    private boolean setRegion=false;//是否需要设置区域
    private boolean setGuide=false;//是否找到辐射值最强的方向
    private String measureVal="";
    LocationService locationService;
    LatLng lastPnt = null;
    private View rootView;

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
        //setTestMsg();
        //initHistoryPoint();
    }

    @Override
    public void onResume() {
        super.onResume();
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
    public void onStop() {
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        longitude=0.0;
        latitude=0.0;
        ColorStatus = 0;
        stopTimer();
        start.setText("开始");
        super.onStop();
    }
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                if (location.getLocType() == BDLocation.TypeServerError) {
                    Toast.makeText(getActivity().getApplicationContext(), "服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    Toast.makeText(getActivity().getApplicationContext(), "网络不同导致定位失败，请检查网络是否通畅", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    Toast.makeText(getActivity().getApplicationContext(), "无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机", Toast.LENGTH_LONG).show();
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
                    Log.i(TAG, "onReceiveLocation: "+"纬度："+Double.toString(latitude)+"；经度：" +
                            Double.toString(longitude) + "；精准度"+ location.getRadius());
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
                    //mCallback.setLocation(latitude,longitude);
                /*setMarker();
                if(!setRegion){
                    if(flag==0){
                        Log.i(TAG, "寻找起点");
                        findStart();
                    }
                    if(flag==1||flag==2){
                        Log.i(TAG, "开始导航");
                        findDirection();
                    }
                    if(flag==3){
                        Log.i(TAG, "沿着方向");
                        alongWithDirection();
                    }
                }
                if(setRegion){
                    double[] now={MainActivity.Latitude,MainActivity.Longitude};
                    createRegion(now,right_dir);
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
        valView=view.findViewById(R.id.get_values);
        start = (Button) view.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if(isStop) {
                        startTimer();
                        start.setText("停止");
                    }else{
                        stopTimer();
                        start.setText("开始");
                    }
                    isStop = !isStop;

                }catch (Exception e) {
                    Log.i(TAG, "onClick: " + e.toString());
                }
            }
        });
        MainActivity.bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                String res = new String(data);
                UpdateUI(res);
            }
        });
    }

    /**
     * @description: 启动计时器
     * @author: lyj
     * @create: 2019/10/14
     **/
    private void startTimer() {
        if(timer == null) {
            timer = new Timer();
        }

        if(task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    // 需要做的事:发送消息
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
        }
        if(timer != null && task != null) {
            timer.schedule(task, 0, 2000); // 0s后执行task,经过2s再次执行
        }
    }

    /**
     * @description: 关闭计时器
     * @author: lyj
     * @create: 2019/10/14
     **/
    private void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        if(task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * @description: handler
     * @author: lyj
     * @create: 2019/10/14
     **/
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String to_send = "GMA";
                //Log.i(TAG, "handleMessage: " + to_send);
                MainActivity.bt.send(to_send.getBytes(), true);
            }
            super.handleMessage(msg);
        }
    };
    /**
     * @description: 更新页面
     * @author: lyj
     * @create: 2019/10/14
     **/
    private void UpdateUI(String data) {
        measureVal=data.substring(5, 9);
        int val=Integer.parseInt(measureVal);
        if(val>MainActivity.errorVal){
            ColorStatus = 4;
            valView.setBackgroundColor(Color.YELLOW);
        }
        else if(val>MainActivity.SeriousVal){
            ColorStatus = 3;
            valView.setBackgroundColor(Color.MAGENTA);
        }
        else if(val>MainActivity.AlarmVal){
            ColorStatus = 2;
            valView.setBackgroundColor(Color.RED);
        }
        else{
            ColorStatus = 1;
            valView.setBackgroundColor(Color.GREEN);
        }
        add(measureVal);
        valView.setText(measureVal);
    }

    /**
     * @description: 存储数据
     * @author: lyj
     * @create: 2019/09/02
     **/
    private void add(String val){
        if(longitude!=0.0&&latitude!=0.0){
            DataMsg msg=new DataMsg(getTime(),val,longitude,latitude,0);
            msg.save();
            DataMsg msg1=DataMsg.find(DataMsg.class,"").get(0);
            Log.i(TAG, "-------val: " + msg1.toString());
        }
    }
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
}


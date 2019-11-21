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
import com.example.detectradiativeresource.Test.MsgTest;
import com.example.detectradiativeresource.bluetooth.library.BluetoothSPP;
import com.example.detectradiativeresource.dao.DataMsg;
import com.example.detectradiativeresource.monitor.trace.LocationService;
import com.example.detectradiativeresource.utils.DBScanUtils;
import com.example.detectradiativeresource.utils.DataHelperUtils;

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
    private Button cal;

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
    public void onPause(){
        super.onPause();
        Message message4 = new Message();
        message4.what = 4;
        handler.sendMessage(message4);
        myStop();
    }
    @Override
    public void onStop() {
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
                    //mCallback.setLocation(latitude,longitude);
                    setMarker();
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
                    }
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
        cal=view.findViewById(R.id.calculate);
        start = (Button) view.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if(isStop) {
                        MainActivity.connectedState=1;
                        startTimer();
                        //start.setText("停止");
                    }else{
                        MainActivity.connectedState=4;
                        startTimer();
                        //stopTimer();
                        //start.setText("开始");
                    }
                    //isStop = !isStop;

                }catch (Exception e) {
                    Log.i(TAG, "onClick: " + e.toString());
                }
            }
        });
        MainActivity.bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(int[] data, String message) {
                handlerReceivedData(data);
            }
        });
        /**
         * @description: DBSCAN测试按钮
         * @author: lyj
         * @create: 2019/09/23
         **/
        cal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double[] ans= DBScanUtils.getTestMsg();
                Toast.makeText(getActivity().getApplicationContext(), "预测坐标为"+ans[0]+":"+ans[1], Toast.LENGTH_LONG).show();
            }
        });
        setTestMsg();
        initHistoryPoint();
    }

    /**
     * @description: 启动计时器，发送数据
     * @author: lyj
     * @create: 2019/10/14
     **/
    private void startTimer() {
        if(timer == null) {
            timer = new Timer();
        }
        switch (MainActivity.connectedState){
            case 1:
                Message message1 = new Message();
                message1.what = 1;
                handler.sendMessage(message1);
                break;
            case 2:
                Message message2 = new Message();
                message2.what = 2;
                handler.sendMessage(message2);
                break;
            case 3:
                task=null;
                task = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                };
                timer.schedule(task, 0,2000);
                break;
            case 4:
                Message message4 = new Message();
                message4.what = 4;
                handler.sendMessage(message4);
                break;
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
     * @description: handler,蓝牙协议核心
     * @author: lyj
     * @create: 2019/11/12
     **/
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    byte[] data1={0x3A,0x30,0x0D,0x0A};
                    //MainActivity.bt.send(data1,true);
                    MainActivity.mBluetoothLeService.writeData(data1);
                    Log.i(TAG, "发送的数据-1！！！！！！: " );
                    break;
                case 2:
                    byte[] data2={0x3A,0x31,0x0D,0x0A};
                    //MainActivity.bt.send(data2,true);
                    MainActivity.mBluetoothLeService.writeData(data2);
                    Log.i(TAG, "发送的数据-2！！！！！！: " );
                    break;
                case 3:
                    byte[] data3={0x3A,0x35,0x0D,0x0A};
                    //MainActivity.bt.send(data3,true);
                    MainActivity.mBluetoothLeService.writeData(data3);
                    Log.i(TAG, "发送的数据-3！！！！！！: " );
                    break;
                case 4:
                    byte[] data4={0x3A,0x32,0x0D,0x0A};
                    //MainActivity.bt.send(data4,true);
                    MainActivity.mBluetoothLeService.writeData(data4);
                    Log.i(TAG, "发送的数据-4！！！！！！: " );
                    break;
            }
            super.handleMessage(msg);
        }
    };
    public void handlerReceivedData(byte[] data){
        for(byte d:data){
            Log.d(TAG, "收到的数据！！！！！！！！！" + d);
        }
    }


    /**
     * @description: 处理接受数据
     * @author: lyj
     * @create: 2019/11/12
     **/
    public void handlerReceivedData(int[] data){
        for(int d:data){
            Log.d(TAG, "收到的数据！！！！！！！！！" + d);
        }
        switch (MainActivity.connectedState){
            case 1:
                if(data.length!=2){
                    Toast.makeText(getActivity().getApplicationContext(), "接受数据错误！与背包连接错误！", Toast.LENGTH_LONG).show();
                }
                else {
                    if(data[0]==0x3A&&data[1]==0x32){
                        Toast.makeText(getActivity().getApplicationContext(), "与背包连接成功", Toast.LENGTH_LONG).show();
                        MainActivity.connectedState=3;
                        start.setText("停止");
                        isStop=false;
                        startTimer();
                        DataHelperUtils.saveDataTotalMsg();
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(), "与背包连接失败", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case 2:
                if(data.length!=2){
                    Toast.makeText(getActivity().getApplicationContext(), "接受数据错误！与背包连接错误！", Toast.LENGTH_LONG).show();
                }
                else{
                    if(data[0]==0x3A&&data[1]==0x31){
                        Toast.makeText(getActivity().getApplicationContext(), "复位启动成功", Toast.LENGTH_LONG).show();
                        MainActivity.connectedState=3;
                        start.setText("停止");
                        isStop=false;
                        startTimer();
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(), "复位启动失败", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case 3:
                UpdateUI(data);
                break;
            case 4:
                if(data.length!=2){
                    Toast.makeText(getActivity().getApplicationContext(), "接受数据错误！与背包连接错误！", Toast.LENGTH_LONG).show();
                }
                else {
                    if(data[0]==0x3A&&data[1]==0x31){
                        Toast.makeText(getActivity().getApplicationContext(), "停止成功", Toast.LENGTH_LONG).show();
                        myStop();
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(), "停止失败", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public void myStop(){
        MainActivity.connectedState=0;
        start.setText("开始");
        isStop=true;
        valView.setText("未测试");
        valView.setBackgroundColor(Color.WHITE);
        stopTimer();
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
     **/
    private void UpdateUI(int[] data) {
        for(int d:data){
            Log.i(TAG, "计算的数据内容是！！！！！！: " + d);
        }
        int val=getVal(data,1,4); //1-4位是获取的数值
        measureVal=String.valueOf(val);
        if(val>MainActivity.errorVal){
            ColorStatus = 4;
            valView.setBackgroundColor(Color.YELLOW);
        }
        else if(val>MainActivity.seriousVal){
            ColorStatus = 3;
            valView.setBackgroundColor(Color.MAGENTA);
            DataHelperUtils.saveLogMsg("报警","报警时间");
        }
        else if(val>MainActivity.alarmVal){
            ColorStatus = 2;
            valView.setBackgroundColor(Color.RED);
            DataHelperUtils.saveLogMsg("报警","警告时间");
        }
        else{
            ColorStatus = 1;
            valView.setBackgroundColor(Color.GREEN);
        }
        DataHelperUtils.saveDataMsg(measureVal,longitude,latitude,0);
        DataHelperUtils.updateDataTotalMsgIsAlarm();
        valView.setText(measureVal);
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

    /**************************************历史轨迹点部分*************************************/

    /**
     * @description: 历史轨迹点的测试
     * @author: lyj
     * @create: 2019/09/27
     **/
    public void setMarker(){
        if(measureVal==null||measureVal.length()==0){
            //Log.i(TAG, "measureVal---------null");
            return;
        }
        if(isRepeatPoint()){
            //Log.i(TAG, "repeated---------null");
            return;
        }
        //Log.i(TAG, "find-----------------------------------------------------------------------go");
        Bundle mBundle = new Bundle();
        mBundle.putString("msg", measureVal);
        LatLng point = new LatLng(latitude, longitude);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_end);
        MarkerOptions option = new MarkerOptions().position(point).icon(bitmap).draggable(true).extraInfo(mBundle).flat(true).alpha(0.5f);
        mBaiduMap.addOverlay(option);
        DataHelperUtils.saveDataMsg(measureVal,longitude,latitude,1);
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
            mBundle.putString("msg", data.getValue());
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
        DataMsg msg=new DataMsg(getTime(),String.valueOf(1111),116.364534,39.970186,1,"否",DataHelperUtils.dataTotalMsg_Id_Now);
        msg.save();
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
     * @description: 绘制初始点
     * @author: lyj
     * @create: 2019/09/10
     **/
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

    /**
     * @description: 开启引路算法
     * @author: lyj
     * @create: 2019/09/10
     **/
    public void findStart() {
        if(MainActivity.testFlag){
            startValue=test.findData();
            test.find();
            if(startValue!=0){
                Toast.makeText(getActivity().getApplicationContext(), String.valueOf(startValue), Toast.LENGTH_LONG).show();
                MainActivity.startLongitude=longitude;
                MainActivity.startLatitude=latitude;
                flag=1;
                pointStart();
            }
        }
    }

    /**
     * @description: 引路算法
     * @author: lyj
     * @create: 2019/09/06
     **/
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
            /*double distance1=DistanceUtil.getDistance(new LatLng(MainActivity.Latitude,MainActivity.Longitude),aim);
            Toast.makeText(getActivity().getApplicationContext(), "距离:"+distance1, Toast.LENGTH_LONG).show();*/
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
                if(now_dir==6){
                    Toast.makeText(getActivity().getApplicationContext(), "完成", Toast.LENGTH_LONG).show();
                    flag=3;
                    now_dir=-1;
                    isColored=!isColored;
                    return;
                }
                now_dir+=2;
                isColored=!isColored;
                flag=1;
                return;
            }
            Toast.makeText(getActivity().getApplicationContext(), "已经到达，请沿着黑色指引路线原路返回", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @description: 目前最大辐射比率方向计算
     * @author: lyj
     * @create: 2019/09/10
     **/
    public void calculateDir() {
        Log.i(TAG, "calculate Dirnb ，flag==1");
        int nextValue=test.findData();
        Toast.makeText(getActivity().getApplicationContext(), "当前辐射值"+nextValue, Toast.LENGTH_LONG).show();
        double distance=DistanceUtil.getDistance(new LatLng(MainActivity.latitude,MainActivity.longitude),new LatLng(MainActivity.startLatitude,MainActivity.startLongitude));
        double incr=(nextValue-startValue)/distance;
        if(incr>maxValueIncr){
            right_dir=now_dir;
        }
    }

    /**
     * @description: 引导沿着辐射比率最大方向
     * @author: lyj
     * @create: 2019/09/10
     **/
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
        int nowValue=test.findData();//判断此时的辐射值是否减小或者到达最大值
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

    /**
     * @description: 绘制区域
     * @author: lyj
     * @create: 2019/09/18
     **/
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
    }
}


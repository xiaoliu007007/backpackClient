package com.example.detectradiativeresource;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.detectradiativeresource.Test.MsgTest;
import com.example.detectradiativeresource.bluetooth.BluetoothFragment;
import com.example.detectradiativeresource.bluetooth.library.BluetoothLeService;
import com.example.detectradiativeresource.bluetooth.library.BluetoothState;
import com.example.detectradiativeresource.bluetooth.library.DeviceScanActivity;
import com.example.detectradiativeresource.data.DataFragment;
import com.example.detectradiativeresource.dao.TestMsg;
import com.example.detectradiativeresource.data.DataTotalFragment;
import com.example.detectradiativeresource.log.LogDetailFragment;
import com.example.detectradiativeresource.log.LogFragment;
import com.example.detectradiativeresource.monitor.LineChartActivity;
import com.example.detectradiativeresource.monitor.MonitorFragment;
import com.example.detectradiativeresource.monitor.trace.LocationService;
import com.example.detectradiativeresource.setting.SettingFragment;
import com.example.detectradiativeresource.utils.BluetoothProtocol;
import com.example.detectradiativeresource.utils.DataHelperUtils;
import com.example.detectradiativeresource.utils.FragmentChangeUtils;
import com.orm.SugarContext;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements DataTotalFragment.DataFragmentChangeListener,
        DataFragment.DataTotalFragmentChangeListener, LogFragment.LogDetailFragmentChangeListener , LogDetailFragment.LogFragmentChangeListener {
    /**
     * 底部导航栏的widdget
     */
    public static final String TAG="MainActivity";
    private RadioGroup mNavGroup;
    private TextView bluetoothMsg;
    private FragmentTransaction mTransaction;
    private View view;
    public static LocationService locationService;
    private FrameLayout mFlLifeRoot;
    public static double longitude;
    public static double latitude;
    public static int total_r_jishu;
    public static int alert_r_jishu;
    public static int total_r_jiliang;
    public static int alert_r_jiliang;
    public static int total_n_jishu;
    public static int alert_n_jishu;
    public static int total_n_jiliang;
    public static int alert_n_jiliang;
    public static int valType=0;//0表示计数率，1表示计量率

    public static double interval;//范围内历史轨迹点的间隔数
    public static double startLatitude;
    public static double startLongitude;
    public static boolean testFlag=false;//是否开启测试
    public static double[][] directions={{0,0.0001},{0.00005,0.00005},{0.0001,0},{0.00005,-0.00005},{0,-0.0001},{-0.00005,-0.00005},{-0.0001,0},{-0.00005,0.00005}};
    public static int maxValue;//人体承受最大辐射值
    public static boolean setRegion=false;//是否找到设置区域
    public static double rightLongitude=0.0;
    public static double rightLatitude=0.0;
    public static int rightDir=-1;
    public static int connectedState=0; //和背包连接状态，1表示处于握手状态，2表示处于复位状态，3表示在测量数据,4表示停止，0表示无状态
    /**
     * 五个Fragments
     */
    Fragment monitorFragemnt, bluetoothFragment, dataTotalFragment,settingFragment, logFragment,dataFragment,logDetailFragment;
    public static final int VIEW_MONITOR_INDEX = 0;
    public static final int VIEW_BLUETOOTH_INDEX = 1;
    public static final int VIEW_DATA_INDEX = 2;
    public static final int VIEW_SETTING_INDEX = 3;
    public static final int VIEW_LOG_INDEX = 4;
    private int temp_position_index = -1;
    public static String IP;//服务器IP地址以及端口


    public static BluetoothLeService mBluetoothLeService;
    private String address;
    public static String receivedState=BluetoothProtocol.NO_STATE;//等待接受协议的状态;
    public static String alertState=BluetoothProtocol.NO_STATE;//报警状态
    public static String settingCollectTimeState=BluetoothProtocol.NO_STATE;//设置采集时间状态
    public static String getSpectrum=BluetoothProtocol.NO_STATE;
    public OnDataListener onDataListener;
    private static final int REQUEST_ENABLE_BT = 1;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            Log.e(TAG, "-----------------mServiceConnection connect--------------");
            mBluetoothLeService.connect(address);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "-----------------mServiceConnection disconnect--------------");
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext()
                    , "连接到" + address
                    , Toast.LENGTH_SHORT).show();
                BluetoothListener myListener=(BluetoothListener)bluetoothFragment;
                myListener.setText("连接成功");
                changeWifi(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //mBluetoothLeService.connect(address);
                receivedState=BluetoothProtocol.NO_STATE;
                Toast.makeText(getApplicationContext()
                    , "连接断开"
                    , Toast.LENGTH_SHORT).show();
                BluetoothListener myListener=(BluetoothListener)bluetoothFragment;
                myListener.setText("连接失败");
                changeWifi(false);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //特征值找到才代表连接成功
            }else if (BluetoothLeService.ACTION_GATT_SERVICES_NO_DISCOVERED.equals(action)){
                //mBluetoothLeService.connect(address);
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
                byte[] data=intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                int [] receivedData=new int[data.length];
                int i=0;
                for(byte d:data){
                    receivedData[i++]= d<0?256 + d:d;
                }
                Fragment currentFragment=getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
                if( currentFragment!=null&&currentFragment instanceof MonitorFragment){
                    handleReceivedDataByMonitorFragment(receivedData);
                }
                if( currentFragment!=null&&currentFragment instanceof SettingFragment){
                    handleReceivedDataBySettingFragment(receivedData);
                }
            }else if (BluetoothLeService.ACTION_WRITE_SUCCESSFUL.equals(action)) {
               /* mSendBytes.setText(sendBytes + " ");
                if (sendDataLen>0)
                {
                    Log.v("log","Write OK,Send again");
                    //onSendBtnClicked();
                }
                else {
                    Log.v("log","Write Finish");
                }*/
            }

        }
    };

    private void changeWifi(final boolean flag){
        new Thread() {
            @Override
            public void run() {
                while (true){
                    try {
                        currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Fragment currentFragment=getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
                    if( currentFragment!=null&&currentFragment instanceof MonitorFragment){
                        MonitorFragment monitor =
                                (MonitorFragment)getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
                        monitor.notifyChangeWifi(flag);
                        Log.e(TAG, "--------------------over----------------------"+flag);
                        return;
                    }
                }
            }
        }.start();

    }
    public interface OnDataListener {
        public void onDataChange(byte[] data);

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_SUCCESSFUL);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_NO_DISCOVERED);
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InputStream in = null;
        Properties property = new Properties();
        try {
            in =getResources().openRawResource(R.raw.config);
            property.load(in);
            maxValue=Integer.valueOf(property.getProperty("maxValue"));
            total_r_jishu=Integer.valueOf(property.getProperty("total_r_jishu"));
            alert_r_jishu=Integer.valueOf(property.getProperty("alert_r_jishu"));
            total_r_jiliang=Integer.valueOf(property.getProperty("total_r_jiliang"));
            alert_r_jiliang=Integer.valueOf(property.getProperty("alert_r_jiliang"));
            total_n_jishu=Integer.valueOf(property.getProperty("total_n_jishu"));
            alert_n_jishu=Integer.valueOf(property.getProperty("alert_n_jishu"));
            total_n_jiliang=Integer.valueOf(property.getProperty("total_n_jiliang"));
            alert_n_jiliang=Integer.valueOf(property.getProperty("alert_n_jiliang"));
            interval=Double.valueOf(property.getProperty("interval"));
            IP=property.getProperty("IP");
        } catch (IOException e) {
            Log.e(TAG, "load properties error",e);
        }finally{
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        locationService = new LocationService(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();

        SugarContext.init(this);
        /**************************************蓝牙配置页面*************************************/
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        /**************************************测试数据录入*************************************/
        if(MainActivity.testFlag){
            if(TestMsg.findById(TestMsg.class,1)==null){
                MsgTest test1=new MsgTest();
                test1.new ReadTxtTask().execute();
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), test1.find(), Toast.LENGTH_LONG).show();
            }
        }
        /**************************************日志信息记录*************************************/
        DataHelperUtils.saveLogMsg("开始","使用时间");
    }

    private void initView() {
        LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        view=inflater.inflate(R.layout.fragment_bluetooth, null);
        bluetoothMsg=view.findViewById(R.id.id_bluetooth_msg);

        mNavGroup = findViewById(R.id.id_navcontent);
        monitorFragemnt = MonitorFragment.getNewInstance();
        bluetoothFragment = BluetoothFragment.getNewInstance();
        dataTotalFragment = DataTotalFragment.getNewInstance();
        dataFragment = DataFragment.getNewInstance();
        settingFragment = SettingFragment.getNewInstance();
        logFragment = LogFragment.getNewInstance();
        logDetailFragment= LogDetailFragment.getNewInstance();
        mFlLifeRoot=findViewById(R.id.id_fragment_content);
        //显示
        mTransaction = getSupportFragmentManager().beginTransaction();
        mTransaction.replace(R.id.id_fragment_content, monitorFragemnt);
        mTransaction.commit();
    }

    public void switchView(View view) {
        switch (view.getId()) {
            case R.id.id_nav_bt_monitor:
                if (temp_position_index != VIEW_MONITOR_INDEX) {
                    //显示
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mFlLifeRoot.removeAllViews();
                    mTransaction.replace(R.id.id_fragment_content, monitorFragemnt);
                    mTransaction.commit();
                }
                temp_position_index = VIEW_MONITOR_INDEX;
                break;
            case R.id.id_nav_bt_bluetooth:
                if (mBluetoothLeService!=null&&mBluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTED) {
                    showbluetoothDialog();
                } else {
                    //显示
                    Intent intent = new Intent(getApplicationContext(), DeviceScanActivity.class);
                    startActivityForResult(intent,BluetoothState.REQUEST_CONNECT_DEVICE);
                }
                mTransaction = getSupportFragmentManager().beginTransaction();
                mFlLifeRoot.removeAllViews();
                mTransaction.replace(R.id.id_fragment_content, bluetoothFragment);
                mTransaction.commit();
                temp_position_index = VIEW_BLUETOOTH_INDEX;
                break;
            case R.id.id_nav_bt_data: //双fragment切换
                if (temp_position_index != VIEW_DATA_INDEX) {
                    //显示
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mFlLifeRoot.removeAllViews();
                    mTransaction.replace(R.id.id_fragment_content, dataTotalFragment);
                    mTransaction.commit();
                }
                temp_position_index = VIEW_DATA_INDEX;
                break;
            case R.id.id_nav_bt_setting:
                if (temp_position_index != VIEW_SETTING_INDEX) {
                    //显示
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mFlLifeRoot.removeAllViews();
                    mTransaction.replace(R.id.id_fragment_content, settingFragment);
                    mTransaction.commit();
                }
                temp_position_index = VIEW_SETTING_INDEX;
                break;
            case R.id.id_nav_bt_log:
                if (temp_position_index != VIEW_LOG_INDEX) {
                    //显示
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mFlLifeRoot.removeAllViews();
                    mTransaction.replace(R.id.id_fragment_content, logFragment);
                    mTransaction.commit();
                }
                temp_position_index = VIEW_LOG_INDEX;
                break;
        }
    }

    public void onStart() {
        super.onStart();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        DataHelperUtils.saveLogMsg("退出","退出时间");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "没有蓝牙权限", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
        if (resultCode == Activity.RESULT_OK){
            address = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
            String deviceName = data.getExtras().getString(BluetoothState.EXTRAS_DEVICE_NAME);
            Log.i(TAG, "--------------address is "+address);
            mBluetoothLeService.connect(address);
            //bt.connect(data);
        }
    }

    //处理已有蓝牙连接，用户强行点击蓝牙配置
    private void showbluetoothDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("已有蓝牙连接");
        normalDialog.setMessage("确定要断开当前连接?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothLeService.disconnect();
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                });
        // 显示
        normalDialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //处理用户强行退出程序
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitDialog();
        }
        return false;
    }

    private void exitDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("提示");
        normalDialog.setMessage("确定要退出当前程序?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                });
        // 显示
        normalDialog.show();
    }

    @Override
    public void changeDataFragment() {
        if(FragmentChangeUtils.isDataTotalMsgFragment){
            Log.i(TAG, "-------------changed1-------------");
            mTransaction = getSupportFragmentManager().beginTransaction();
            mFlLifeRoot.removeAllViews();
            mTransaction.replace(R.id.id_fragment_content, dataTotalFragment);
            mTransaction.commit();
        }else{
            mTransaction = getSupportFragmentManager().beginTransaction();
            mFlLifeRoot.removeAllViews();
            mTransaction.replace(R.id.id_fragment_content, dataFragment);
            mTransaction.commit();
        }
    }

    @Override
    public void changeLogDetailFragment() {
        if(FragmentChangeUtils.isLogFragment){
            Log.i(TAG, "-------------changed1-------------");
            mTransaction = getSupportFragmentManager().beginTransaction();
            mFlLifeRoot.removeAllViews();
            mTransaction.replace(R.id.id_fragment_content, logFragment);
            mTransaction.commit();
        }else{
            mTransaction = getSupportFragmentManager().beginTransaction();
            mFlLifeRoot.removeAllViews();
            mTransaction.replace(R.id.id_fragment_content, logDetailFragment);
            mTransaction.commit();
        }
    }

    public interface BluetoothListener{
        public void setText(String code);
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

    /**
     * @description: 协议的发送
     * @author: lyj
     * @create: 2019/11/26
     **/
    public static void send(String type,byte[] data){
        switch (type){
            case BluetoothProtocol.SHAKE_HANDS:
                byte[] send_shake_hands={0x68,0x30,0x0D,0x0A};
                mBluetoothLeService.writeData(send_shake_hands);
                receivedState=BluetoothProtocol.SHAKE_HANDS;
                Log.i(TAG, "握手中" );
                break;
            case BluetoothProtocol.START_MEASURE:
                byte[] send_start_measure={0x68,0x39,0x0D,0x0A};
                mBluetoothLeService.writeData(send_start_measure);
                receivedState=BluetoothProtocol.START_MEASURE;
                Log.i(TAG, "启动测量中" );
                break;
            case BluetoothProtocol.GET_DATA:
                byte[] send_get_data={0x68,0x31,0x0D,0x0A};
                mBluetoothLeService.writeData(send_get_data);
                receivedState=BluetoothProtocol.GET_DATA;
                Log.i(TAG, "获取数据中" );
                break;
            case BluetoothProtocol.STOP_MEASURE:
                byte[] send_stop_data={0x68,0x33,0x0D,0x0A};
                mBluetoothLeService.writeData(send_stop_data);
                receivedState=BluetoothProtocol.STOP_MEASURE;
                Log.i(TAG, "停止测量中" );
            case BluetoothProtocol.ALERT_START:
                byte[] send_alert_start={0x68,0x33,0x0D,0x0A};
                mBluetoothLeService.writeData(send_alert_start);
                Log.i(TAG, "收到报警" );
                break;
            case BluetoothProtocol.ALERT_CLOSE:
                byte[] send_alert_close={0x68,0x42,0x0D,0x0A};
                mBluetoothLeService.writeData(send_alert_close);
                alertState=BluetoothProtocol.ALERT_CLOSE;
                Log.i(TAG, "关闭报警" );
                break;

            case BluetoothProtocol.SETTING_COLLECT_TIME:
                byte[] send_collect_time=new byte[5];
                send_collect_time[0]=0x68;
                send_collect_time[1]=0x36;
                send_collect_time[2]=data[0];
                send_collect_time[3]=0x0D;
                send_collect_time[4]=0x0A;
                mBluetoothLeService.writeData(send_collect_time);
                settingCollectTimeState=BluetoothProtocol.SETTING_COLLECT_TIME;
                Log.i(TAG, "发送设置采集时间" );
                break;
            case BluetoothProtocol.GET_SPECTRUM:
                byte[] send_get_spectrum={0x68,0x32,0x0D,0x0A};
                mBluetoothLeService.writeData(send_get_spectrum);
                getSpectrum=BluetoothProtocol.GET_SPECTRUM;
                Log.i(TAG, "获取能谱" );
                break;
        }
    }

    /**
     * @description: MonitorFragment处理收到的数据
     * @author: lyj
     * @create: 2019/11/26
     **/
    public void handleReceivedDataByMonitorFragment(int[] data){
        boolean flag=false;//当前命令是否找到对应内容
        MonitorFragment monitor =
                (MonitorFragment)getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
        if(data.length==5){
            if(data[0]==0x68&&data[1]==0x38&&data[3]==0x0D&&data[4]==0x0A){
                send(BluetoothProtocol.ALERT_START,new byte[]{});
                alertState=BluetoothProtocol.ALERT_START;
                monitor.handlerReceivedData(BluetoothProtocol.ALERT_START,data);
                return;
            }
        }
        if(alertState==BluetoothProtocol.ALERT_CLOSE){
            int[] aim_alert_close_ok={0x68,0x31,0x0D,0x0A};
            if(Arrays.equals(aim_alert_close_ok,data)){
                alertState=BluetoothProtocol.ALERT_CLOSE_OK;
                monitor.handlerReceivedData(alertState,new int[]{});
                return;
            }
            int[] aim_alert_close_failed={0x68,0x30,0x0D,0x0A};
            if(Arrays.equals(aim_alert_close_failed,data)){
                alertState=BluetoothProtocol.ALERT_CLOSE_FAILED;
                monitor.handlerReceivedData(alertState,new int[]{});
                return;
            }
        }
        if(getSpectrum==BluetoothProtocol.GET_SPECTRUM&&data.length==13){
            //monitor.handlerReceivedData(getSpectrum,data);
            jumpToSpectrum(data);
            return;
        }
        switch (receivedState){
            case BluetoothProtocol.SHAKE_HANDS:
                int[] aim_shake_hands_ok={0x68,0x32,0x0D,0x0A};
                if(Arrays.equals(aim_shake_hands_ok,data)){
                    receivedState=BluetoothProtocol.SHAKE_HANDS_OK;
                    flag=true;
                }
                else {
                    int[] aim_shake_hands_failed={0x68,0x30,0x0D,0x0A};
                    if(Arrays.equals(aim_shake_hands_failed,data)){
                        receivedState=BluetoothProtocol.SHAKE_HANDS_FAILED;
                        flag=true;
                    }
                }
                if(flag){
                    monitor.handlerReceivedData(receivedState,new int[]{});
                    return;
                }
                break;
            case BluetoothProtocol.START_MEASURE:
                int[] aim_start_measure_ok={0x68,0x31,0x0D,0x0A};
                if(Arrays.equals(aim_start_measure_ok,data)){
                    receivedState=BluetoothProtocol.START_MEASURE_OK;
                    flag=true;
                }
                else{
                    int[] aim_start_measure_failed={0x68,0x30,0x0D,0x0A};
                    if(Arrays.equals(aim_start_measure_failed,data)){
                        receivedState=BluetoothProtocol.START_MEASURE_FAILED;
                        flag=true;
                    }
                }
                if(flag){
                    monitor.handlerReceivedData(receivedState,new int[]{});
                    return;
                }
                break;
            case BluetoothProtocol.GET_DATA:
                if(data.length==20){
                    flag=true;
                    monitor.handlerReceivedData(receivedState,data);
                    return;
                }
                break;
            case BluetoothProtocol.STOP_MEASURE:
                int[] aim_stop_measure_ok={0x68,0x31,0x0D,0x0A};
                if(Arrays.equals(aim_stop_measure_ok,data)){
                    receivedState=BluetoothProtocol.STOP_MEASURE_OK;
                    flag=true;
                }
                else {
                    int[] aim_stop_measure_failed={0x68,0x30,0x0D,0x0A};
                    if(Arrays.equals(aim_stop_measure_failed,data)){
                        receivedState=BluetoothProtocol.STOP_MEASURE_FAILED;
                        flag=true;
                    }
                }
                if(flag){
                    monitor.handlerReceivedData(receivedState,new int[]{});
                    return;
                }
                break;
        }
        Log.i(TAG, "我出来啦！！！！" );
    }

    /**
     * @description: SettingFragment处理收到的数据
     * @author: lyj
     * @create: 2019/11/27
     **/
    public void handleReceivedDataBySettingFragment(int[] data){
        boolean flag=false;
        SettingFragment setting =
                (SettingFragment)getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
        if(receivedState==BluetoothProtocol.SHAKE_HANDS){
            int[] aim_shake_hands_ok={0x68,0x32,0x0D,0x0A};
            if(Arrays.equals(aim_shake_hands_ok,data)){
                receivedState=BluetoothProtocol.SHAKE_HANDS_OK;
                flag=true;
            }
            else {
                int[] aim_shake_hands_failed={0x68,0x30,0x0D,0x0A};
                if(Arrays.equals(aim_shake_hands_failed,data)){
                    receivedState=BluetoothProtocol.SHAKE_HANDS_FAILED;
                    flag=true;
                }
            }
            if(flag){
                setting.handlerReceivedData(receivedState,new int[]{});
                return;
            }
        }
        else if(settingCollectTimeState==BluetoothProtocol.SETTING_COLLECT_TIME){
            int[] aim_collect_time_ok={0x68,0x31,0x0D,0x0A};
            if(Arrays.equals(aim_collect_time_ok,data)){
                settingCollectTimeState=BluetoothProtocol.SETTING_COLLECT_TIME_OK;
                flag=true;
            }
            else {
                int[] aim_collect_time_failed={0x68,0x30,0x0D,0x0A};
                if(Arrays.equals(aim_collect_time_failed,data)){
                    settingCollectTimeState=BluetoothProtocol.SETTING_COLLECT_TIME_FAILED;
                    flag=true;
                }
            }
            if(flag){
                setting.handlerReceivedData(settingCollectTimeState,new int[]{});
                return;
            }
        }
    }

    /**
     * @description: 跳转到能谱Activity展示
     * @author: lyj
     * @create: 2019/11/27
     **/
    private void jumpToSpectrum(int[] data){
        int[] nums=new int[(data.length-3)/2];
        for(int i=0;i<nums.length;i++){
            nums[i]=BluetoothProtocol.getVal(data,2*i+1,2*i+2);
        }
        MainActivity.getSpectrum=BluetoothProtocol.NO_STATE;
        Intent intent=new Intent(MainActivity.this, LineChartActivity.class);
        intent.putExtra("data",nums);
        startActivity(intent);
    }

}

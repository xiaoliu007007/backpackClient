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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.detectradiativeresource.Test.MsgTest;
import com.example.detectradiativeresource.bluetooth.BluetoothFragment;
import com.example.detectradiativeresource.bluetooth.library.BluetoothLeService;
import com.example.detectradiativeresource.bluetooth.library.BluetoothState;
import com.example.detectradiativeresource.bluetooth.library.DeviceScanActivity;
import com.example.detectradiativeresource.dao.DataMsg;
import com.example.detectradiativeresource.dao.DataTotalMsg;
import com.example.detectradiativeresource.data.DataFragment;
import com.example.detectradiativeresource.dao.TestMsg;
import com.example.detectradiativeresource.data.DataTotalFragment;
import com.example.detectradiativeresource.log.LogDetailFragment;
import com.example.detectradiativeresource.log.LogFragment;
import com.example.detectradiativeresource.monitor.LineChartActivity;
import com.example.detectradiativeresource.monitor.MonitorFragment;
import com.example.detectradiativeresource.monitor.trace.LocationService;
import com.example.detectradiativeresource.setting.SettingFragment;
import com.example.detectradiativeresource.spectrum.SpectrumFragment;
import com.example.detectradiativeresource.utils.BluetoothProtocol;
import com.example.detectradiativeresource.utils.CrashHandler;
import com.example.detectradiativeresource.utils.DataHelperUtils;
import com.example.detectradiativeresource.utils.FragmentChangeUtils;
import com.orm.SugarContext;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements DataTotalFragment.DataFragmentChangeListener,
        DataFragment.DataTotalFragmentChangeListener, LogFragment.LogDetailFragmentChangeListener , LogDetailFragment.LogFragmentChangeListener {
    /**
     * 底部导航栏的widdget
     */
    public static final String TAG="MainActivity";
    private RadioGroup mNavGroup;
    //private TextView bluetoothMsg;
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
    public static int Nai_jishu;
    public static int Nai_jiliang;
    public static int GM_jishu;
    public static int GM_jiliang;
    public static int n_jishu;
    public static int n_jiliang;
    public static int valType=1;//1表示计数率，2表示计量率
    public static boolean isAlertOpen = true; //是否是打开报警状态
    public static int closeAlertTime=30;
    public static boolean isSendCloseAlert=false;//没有发送过关闭timer
    public static Timer timer = null;
    public static TimerTask task = null;
    public static Timer connectTimer = null;
    public static TimerTask connectTask = null;
    public static String connectedAddress=null; //默认地址
    public static String connectedStateMsg="蓝牙未连接";//蓝牙连接信息
    private static RadioButton bluetoothBtn;//蓝牙按钮
    public static int spectrumTimeInterval=20;//能谱采集时间间隔
    public static boolean isAlertMusic=true;//是否声音报警
    public static boolean isNoAlert=false;//中途是否出现正常数值

    public static double interval;//范围内历史轨迹点的间隔数
    public static boolean testFlag=false;//是否开启测试
    public static boolean openNavi=false;//是否开启智能导航
    public static double[][] directions={{0,0.0001},{0.00005,0.00005},{0.0001,0},{0.00005,-0.00005},{0,-0.0001},{-0.00005,-0.00005},{-0.0001,0},{-0.00005,0.00005}};
    public static int maxValue;//人体承受最大辐射值
    public static boolean setRegion=false;//是否找到设置区域
    public static double rightLongitude=0.0;
    public static double rightLatitude=0.0;
    public static int rightDir=-1;
    /*public static int NaI_jishu_value;//当前点辐射值数据
    public static int NaI_jiliang_value;*/

    public static int connectedState=0; //和背包连接状态，1表示处于握手状态，2表示处于复位状态，3表示在测量数据,4表示停止，0表示无状态
    /**
     * 五个Fragments
     */
    Fragment monitorFragemnt, bluetoothFragment, dataTotalFragment,settingFragment, logFragment,dataFragment,logDetailFragment,spectrumFragment;
    public static final int VIEW_MONITOR_INDEX = 0;
    public static final int VIEW_BLUETOOTH_INDEX = 1;
    public static final int VIEW_DATA_INDEX = 2;
    public static final int VIEW_SETTING_INDEX = 3;
    public static final int VIEW_LOG_INDEX = 4;
    public static final int VIEW_SPECTRUM_INDEX = 5;
    private int temp_position_index = -1;
    public static String IP;//服务器IP地址以及端口
    private PowerManager.WakeLock mWakeLock;


    public static BluetoothLeService mBluetoothLeService;
    public static boolean secondConnect=false;//是否再次重连
    public static boolean connectFlag=false;//蓝牙连接状态
    ArrayList<Integer> receivedList=new ArrayList<>();//接受数据的list
    public static boolean autoFlag=false;//是否自动重连
    private String address;
    public static String receivedState=BluetoothProtocol.NO_STATE;//等待接受协议的状态;
    public static String alertState=BluetoothProtocol.NO_STATE;//报警状态
    public static String settingCollectTimeState=BluetoothProtocol.NO_STATE;//设置采集时间状态
    public static String settingAmendState=BluetoothProtocol.NO_STATE;//NaI修正状态
    public static String settingCollectBGState=BluetoothProtocol.NO_STATE;//强制采集本底状态
    public static String settingThresholdHLState=BluetoothProtocol.NO_STATE;//高低本底阈值状态
    public static String settingThresholdAlertState=BluetoothProtocol.NO_STATE;//报警阈值设定的状态

    public static String settingGetDataPart1State=BluetoothProtocol.NO_STATE;//获取第一部分参数内容状态
    public static String settingGetDataPart2State=BluetoothProtocol.NO_STATE;//获取第二部分参数内容状态
    public static String settingSetDataPart1State=BluetoothProtocol.NO_STATE;//设置第一部分参数内容状态
    public static String settingSetDataPart2State=BluetoothProtocol.NO_STATE;//设置第一部分参数内容状态
    private static byte[] send_set_part2_2=new byte[4];
    private static boolean isSendAgain=false;
    private static boolean isFirstRead=true;//第一次获取参数设置

    public static String getSpectrum=BluetoothProtocol.NO_STATE;//能谱获取状态

    public static int collectBGVal;//强制本底采集内容
    public OnDataListener onDataListener;
    private static final int REQUEST_ENABLE_BT = 1;

    /**************************************导航算法部分*************************************/
    public static boolean isNaviStart=false;//是否是导航开始状态
    public static boolean isNaviEnd=false;//是否是导航中状态
    public static double startLatitude;
    public static double startLongitude;
    public static int startNaIJiShu;
    public static double incrByValue;
    public static double incrByLongitude;
    public static double incrByLatitude;

    public static boolean isEnterRegion=false;
    public static boolean isPredictStart=false;
    public static boolean isPredictEnd=false;
    public static int predictSize=10;//采集预测数据数量

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.e(TAG, "-----------------mServiceConnection connect--------------");
            mBluetoothLeService.connect(connectedAddress);
            isFirstRead=true;
            //address=connectedAddress;
            //mBluetoothLeService.connect(address);
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
                        , "连接到" + connectedAddress
                        , Toast.LENGTH_SHORT).show();
                connectedStateMsg="连接成功";
                Fragment currentFragment=getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
                if( currentFragment!=null&&currentFragment instanceof BluetoothFragment){
                    BluetoothListener myListener=(BluetoothListener)bluetoothFragment;
                    myListener.setText(connectedStateMsg);
                }
                bluetoothBtn.setText("蓝牙已连接");
                //startConnectTimer();
                startTimer(1);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //mBluetoothLeService.connect(address);
                receivedState=BluetoothProtocol.NO_STATE;
                Toast.makeText(getApplicationContext()
                        , "连接断开"
                        , Toast.LENGTH_SHORT).show();
                MainActivity.receivedState=BluetoothProtocol.NO_STATE;
                connectedStateMsg="蓝牙未连接";
                Fragment currentFragment=getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
                if( currentFragment!=null&&currentFragment instanceof BluetoothFragment){
                    BluetoothListener myListener=(BluetoothListener)bluetoothFragment;
                    myListener.setText(connectedStateMsg);
                }
                bluetoothBtn.setText("蓝牙未连接");
                receivedState=BluetoothProtocol.NO_STATE;
                //stopConnectTimer();
                stopTimer();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //特征值找到才代表连接成功
            }else if (BluetoothLeService.ACTION_GATT_SERVICES_NO_DISCOVERED.equals(action)){
                //mBluetoothLeService.connect(address);
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //Toast.makeText(getApplicationContext(), "进入1", Toast.LENGTH_SHORT).show();
                //displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
                byte[] data=intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                byte[] aim_shake_hands_ok={0x68,0x32,0x0D,0x0A};
                if(Arrays.equals(aim_shake_hands_ok,data)){
                    receivedState=BluetoothProtocol.SHAKE_HANDS_OK;
                    //stopConnectTimer();
                    stopTimer();
                    MainActivity.send(BluetoothProtocol.SETTING_GET_DATA_PART1,new byte[]{});
                    return;
                }
                if(isFirstRead&&settingGetDataPart1State==BluetoothProtocol.SETTING_GET_DATA_PART1&&data.length==6){
                    settingGetDataPart1State=BluetoothProtocol.SETTING_GET_DATA_PART1_OK;
                    spectrumTimeInterval=data[1];
                    isFirstRead=false;
                    return;

                }
                //Toast.makeText(getApplicationContext(), "进入2", Toast.LENGTH_SHORT).show();
                Log.v("log","----------------------------received enter------------------"+data.length);
                int i=0;
                /*while(i!=data.length&&(receivedState==BluetoothProtocol.SHAKE_HANDS||receivedState==BluetoothProtocol.NO_STATE)&&data[i]==0){
                    i++;
                    Log.v("log","----------------------------遇到0了------------------");
                }*/
                while(i<data.length-1){
                    receivedList.add(data[i]<0?256 + data[i]:data[i]);
                    if(data[i]==0x0D&&data[i+1]==0x0A){
                        i++;
                        receivedList.add(data[i]<0?256 + data[i]:data[i]);
                        int[] receivedData=new int[receivedList.size()];
                        for(int n=0;n<receivedData.length;n++){
                            receivedData[n]=receivedList.get(n);
                            Log.i("log","list is "+n+" is "+receivedData[n]);
                        }
                        receivedList.clear();
                        Log.i("log","----------------------------received exit-------------------");
                        Fragment currentFragment=getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
                        if( currentFragment!=null&&currentFragment instanceof MonitorFragment){
                            handleReceivedDataByMonitorFragment(receivedData);
                        }
                        if( currentFragment!=null&&currentFragment instanceof SettingFragment){
                            handleReceivedDataBySettingFragment(receivedData);
                        }
                        if( currentFragment!=null&&currentFragment instanceof SpectrumFragment){
                            handleReceivedDataBySpectrumFragment(receivedData);
                        }

                    }
                    i++;
                }
                if(i==data.length-1){
                    receivedList.add(data[i]<0?256 + data[i]:data[i]);
                }
                /*Log.v("log","list length is "+receivedList.size());
                for(int i=0;i<receivedList.size();i++){
                    Log.i("log","list is "+i+" is "+receivedList.get(i));
                }*/
                if(data.length<2||data[data.length-2]!=0x0D||data[data.length-1]!=0x0A){
                    return;
                }
                /*int[] receivedData=new int[receivedList.size()];
                for(int i=0;i<receivedData.length;i++){
                    receivedData[i]=receivedList.get(i);
                }
                receivedList.clear();
                Log.i("log","----------------------------received exit-------------------");
                Fragment currentFragment=getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
                if( currentFragment!=null&&currentFragment instanceof MonitorFragment){
                    handleReceivedDataByMonitorFragment(receivedData);
                }
                if( currentFragment!=null&&currentFragment instanceof SettingFragment){
                    handleReceivedDataBySettingFragment(receivedData);
                }*/
            }else if (BluetoothLeService.ACTION_WRITE_SUCCESSFUL.equals(action)) {
                if(isSendAgain){
                    mBluetoothLeService.writeData(send_set_part2_2);
                    isSendAgain=false;
                }
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
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.i(TAG, "--------------onResume ---------------");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        unregisterReceiver(mGattUpdateReceiver);
        //stopConnectTimer();
        stopTimer();
        Log.i(TAG, "--------------onPause ---------------");
    }



    private void acquireWakeLock() {
        if(mWakeLock == null) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    this.getClass().getCanonicalName());
            mWakeLock.acquire();

        }

    }

    private void releaseWakeLock() {
        if(mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CrashHandler.getInstance().init(getApplicationContext());

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
            connectedAddress=property.getProperty("bluetooth_address");
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
        /*Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);*/
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
        //bluetoothMsg=view.findViewById(R.id.id_bluetooth_msg);

        mNavGroup = (RadioGroup)findViewById(R.id.id_navcontent);
        monitorFragemnt = MonitorFragment.getNewInstance();
        bluetoothFragment = BluetoothFragment.getNewInstance();
        dataTotalFragment = DataTotalFragment.getNewInstance();
        dataFragment = DataFragment.getNewInstance();
        settingFragment = SettingFragment.getNewInstance();
        logFragment = LogFragment.getNewInstance();
        logDetailFragment= LogDetailFragment.getNewInstance();
        spectrumFragment= SpectrumFragment.getNewInstance();
        mFlLifeRoot=(FrameLayout)findViewById(R.id.id_fragment_content);
        bluetoothBtn=(RadioButton)findViewById(R.id.id_nav_bt_bluetooth);
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
                setAutoFlagToTrue();
                /*if (temp_position_index != VIEW_BLUETOOTH_INDEX) {
                    //显示
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mFlLifeRoot.removeAllViews();
                    mTransaction.replace(R.id.id_fragment_content, bluetoothFragment);
                    mTransaction.commit();
                }
                temp_position_index = VIEW_BLUETOOTH_INDEX;
                setAutoFlagToTrue();*/
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
                setAutoFlagToTrue();
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
                setAutoFlagToTrue();
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
                setAutoFlagToTrue();
                break;
            case R.id.id_nav_bt_spectrum:
                if (temp_position_index != VIEW_SPECTRUM_INDEX) {
                    //显示
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mFlLifeRoot.removeAllViews();
                    mTransaction.replace(R.id.id_fragment_content, spectrumFragment);
                    mTransaction.commit();
                }
                temp_position_index = VIEW_SPECTRUM_INDEX;
                setAutoFlagToTrue();
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
            //address = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
            connectedAddress = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
            String deviceName = data.getExtras().getString(BluetoothState.EXTRAS_DEVICE_NAME);
            Log.i(TAG, "--------------address is---------------"+address);
            //mBluetoothLeService.connect(address);
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
        //public void change();
    }

    public interface SpectrumListener{
        public void notifyDataChanged(int[] data);
    }

    /**
     * @description: 获取当前时间
     * @author: lyj
     * @create: 2019/09/02
     **/
    private static String getTime() {
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
        if(mBluetoothLeService==null){
            return;
        }
        switch (type){
            case BluetoothProtocol.SHAKE_HANDS:
                byte[] send_shake_hands={0x68,0x30,0x0D,0x0A};
                mBluetoothLeService.writeData(send_shake_hands);
                receivedState=BluetoothProtocol.SHAKE_HANDS;
                Log.i(TAG, "握手中" );
                break;
            /*case BluetoothProtocol.START_MEASURE:
                byte[] send_start_measure={0x68,0x39,0x0D,0x0A};
                mBluetoothLeService.writeData(send_start_measure);
                receivedState=BluetoothProtocol.START_MEASURE;
                Log.i(TAG, "启动测量中" );
                break;*/
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
                break;
            case BluetoothProtocol.ALERT_START:
                byte[] send_alert_start={0x68,0x00,0x0D,0x0A};
                int n1=0xA1;
                send_alert_start[1]=(byte)n1;
                mBluetoothLeService.writeData(send_alert_start);
                Log.i(TAG, "收到报警" +getTime());
                break;
            case BluetoothProtocol.ALERT_CLOSE:
                byte[] send_alert_close=new byte[5];
                send_alert_close[0]=0x68;
                send_alert_close[1]=0x42;
                send_alert_close[2]=data[0];
                send_alert_close[3]=0x0D;
                send_alert_close[4]=0x0A;
                mBluetoothLeService.writeData(send_alert_close);
                alertState=BluetoothProtocol.ALERT_CLOSE;
                /*if(!MainActivity.isSendCloseAlert){
                    MainActivity.isSendCloseAlert=true;
                }*/
                //stopTimer();
                Log.i(TAG, "关闭报警" );
                break;
            case BluetoothProtocol.ALERT_OPEN:
                byte[] send_alert_open={0x68,0x42,0x00,0x0D,0x0A};
                mBluetoothLeService.writeData(send_alert_open);
                alertState=BluetoothProtocol.ALERT_OPEN;
                /*if(MainActivity.isSendCloseAlert){
                    MainActivity.isSendCloseAlert=false;
                }*/
                //stopTimer();
                Log.i(TAG, "开启报警" );
                break;

            case BluetoothProtocol.SETTING_GET_DATA_PART1:
                byte[] send_get_part1={0x68,0x43,0x0D,0x0A};
                mBluetoothLeService.writeData(send_get_part1);
                settingGetDataPart1State=BluetoothProtocol.SETTING_GET_DATA_PART1;
                Log.i(TAG, "获取参数设置一" );
                break;
            case BluetoothProtocol.SETTING_GET_DATA_PART2:
                byte[] send_get_part2=new byte[6];
                send_get_part2[0]=0x68;
                send_get_part2[1]=0x40;
                send_get_part2[2]=data[0];
                send_get_part2[3]=(byte)MainActivity.valType;
                send_get_part2[4]=0x0D;
                send_get_part2[5]=0x0A;
                mBluetoothLeService.writeData(send_get_part2);
                settingGetDataPart2State=BluetoothProtocol.SETTING_GET_DATA_PART2;
                Log.i(TAG, "获取参数设置二-type is"+ data[0]);
                break;
            case BluetoothProtocol.SETTING_SET_DATA_PART1:
                byte[] send_set_part1=new byte[7];
                send_set_part1[0]=0x68;
                send_set_part1[1]=0x36;
                send_set_part1[2]=data[0];
                send_set_part1[3]=data[1];
                send_set_part1[4]=data[2];
                send_set_part1[5]=0x0D;
                send_set_part1[6]=0x0A;
                mBluetoothLeService.writeData(send_set_part1);
                settingSetDataPart1State=BluetoothProtocol.SETTING_SET_DATA_PART1;
                Log.i(TAG, "设置参数一" );
                for(byte n:send_set_part1){
                    Log.i(TAG, "data is"+n);
                }
                break;
            case BluetoothProtocol.SETTING_SET_DATA_PART2:
                byte[] send_set_part2_1=new byte[20];
                send_set_part2_1[0]=0x68;
                send_set_part2_1[1]=0x34;
                for(int i=0;i<18;i++){
                    send_set_part2_1[i+2]=data[i];
                }
                send_set_part2_2[0]=data[18];
                send_set_part2_2[1]=(byte)MainActivity.valType;
                send_set_part2_2[2]=0x0D;
                send_set_part2_2[3]=0x0A;
                mBluetoothLeService.writeData(send_set_part2_1);
                isSendAgain=true;
                settingSetDataPart2State=BluetoothProtocol.SETTING_SET_DATA_PART2;
                Log.i(TAG, "设置参数二" );
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
        /*if(data.length==5){
            if(data[0]==0x68&&data[1]==0x38&&data[3]==0x0D&&data[4]==0x0A){
                send(BluetoothProtocol.ALERT_START,new byte[]{});
                alertState=BluetoothProtocol.ALERT_START;
                monitor.handlerReceivedData(BluetoothProtocol.ALERT_START,data);
                if(!MainActivity.isSendCloseAlert){
                    timer = new Timer();
                    task = new TimerTask() {
                        @Override
                        public void run() {
                            // 需要做的事:发送消息
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }
                    };
                    timer.schedule(task, MainActivity.closeAlertTime*1000);
                    MainActivity.isSendCloseAlert=!MainActivity.isSendCloseAlert;
                }
                return;
            }
        }*/
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
        if(alertState==BluetoothProtocol.ALERT_OPEN){
            int[] aim_alert_close_ok={0x68,0x31,0x0D,0x0A};
            if(Arrays.equals(aim_alert_close_ok,data)){
                alertState=BluetoothProtocol.ALERT_OPEN_OK;
                monitor.handlerReceivedData(alertState,new int[]{});
                return;
            }
            int[] aim_alert_close_failed={0x68,0x30,0x0D,0x0A};
            if(Arrays.equals(aim_alert_close_failed,data)){
                alertState=BluetoothProtocol.ALERT_OPEN_FAILED;
                monitor.handlerReceivedData(alertState,new int[]{});
                return;
            }
        }
        /*//获取能谱
        if(getSpectrum==BluetoothProtocol.GET_SPECTRUM&&data.length==2051){
            //monitor.handlerReceivedData(getSpectrum,data);
            jumpToSpectrum(data);
            return;
        }*/
        if(settingSetDataPart1State==BluetoothProtocol.SETTING_COLLECT_BG_TIME_CONTINUE&&data.length==8){
            collectBGVal=BluetoothProtocol.getVal(data,2,5);
            settingSetDataPart1State=BluetoothProtocol.NO_STATE;
            Toast.makeText(getApplicationContext(), "本底值为"+collectBGVal, Toast.LENGTH_SHORT).show();
            return;
        }
        switch (receivedState){
            /*case BluetoothProtocol.SHAKE_HANDS:
                int[] aim_shake_hands_ok={0x68,0x32,0x0D,0x0A};
                if(Arrays.equals(aim_shake_hands_ok,data)){
                    receivedState=BluetoothProtocol.SHAKE_HANDS_OK;
                    stopConnectTimer();
                    flag=true;
                }
                else {
                    int[] aim_shake_hands_failed={0x68,0x30,0x0D,0x0A};
                    if(Arrays.equals(aim_shake_hands_failed,data)){
                        receivedState=BluetoothProtocol.SHAKE_HANDS_FAILED;
                        flag=true;
                    }
                }
                *//*if(flag){
                    monitor.handlerReceivedData(receivedState,new int[]{});
                    return;
                }*//*
                break;*/
            /*case BluetoothProtocol.START_MEASURE:
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
                break;*/
            case BluetoothProtocol.GET_DATA:
                if(data.length==29){
                    Log.i(TAG, "---------isAlertOpen is "+isAlertOpen);
                    Log.i(TAG, "---------isSendCloseAlert is "+isSendCloseAlert);
                    Log.i(TAG, "---------isNoAlert is "+isNoAlert);
                    flag=true;
                    monitor.handlerReceivedData(receivedState,data);
                    if(valType==1&&data[23]!=0){
                        handlerAlert(data);
                    }
                    else if(valType==2&&data[25]!=0){
                        handlerAlert(data);
                    }
                    /*if(data[19]!=0){
                        handlerAlert(data);
                    }*/
                    else{
                        isAlertOpen=true;
                        if(isSendCloseAlert){
                            isNoAlert=true;
                            stopAlertTimer();
                        }
                        isSendCloseAlert=false;
                    }
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
                    MainActivity.receivedState=BluetoothProtocol.NO_STATE;
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
        Log.i(TAG, "data len is "+data.length);
        //Log.i(TAG, "---------setting----------- ");
        boolean flag=false;
        SettingFragment setting =
                (SettingFragment)getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
        if(receivedState==BluetoothProtocol.STOP_MEASURE){
            int[] aim_stop_measure_ok={0x68,0x31,0x0D,0x0A};
            if(Arrays.equals(aim_stop_measure_ok,data)){
                //receivedState=BluetoothProtocol.STOP_MEASURE_OK;
                receivedState=BluetoothProtocol.NO_STATE;
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
                return;
            }
        }
        /*if(receivedState==BluetoothProtocol.SHAKE_HANDS){
            int[] aim_shake_hands_ok={0x68,0x32,0x0D,0x0A};
            if(Arrays.equals(aim_shake_hands_ok,data)){
                receivedState=BluetoothProtocol.SHAKE_HANDS_OK;
                stopConnectTimer();
                flag=true;
            }
            else {
                int[] aim_shake_hands_failed={0x68,0x30,0x0D,0x0A};
                if(Arrays.equals(aim_shake_hands_failed,data)){
                    receivedState=BluetoothProtocol.SHAKE_HANDS_FAILED;
                    flag=true;
                }
            }
            *//*if(flag){
                setting.handlerReceivedData(receivedState,new int[]{});
                return;
            }*//*
        }*/

        if(settingSetDataPart1State==BluetoothProtocol.SETTING_COLLECT_BG_TIME_CONTINUE&&data.length==8){
            collectBGVal=BluetoothProtocol.getVal(data,2,5);
            settingSetDataPart1State=BluetoothProtocol.NO_STATE;
            Toast.makeText(getApplicationContext(), "本底值为"+collectBGVal, Toast.LENGTH_SHORT).show();
            return;
        }
        if(settingGetDataPart1State==BluetoothProtocol.SETTING_GET_DATA_PART1&&data.length==6){
            settingGetDataPart1State=BluetoothProtocol.SETTING_GET_DATA_PART1_OK;
            setting.handlerReceivedData(settingGetDataPart1State,data);
            //Log.i(TAG, "收到读取数据一！！！！" );
            return;
        }
        if(settingGetDataPart2State==BluetoothProtocol.SETTING_GET_DATA_PART2&&data.length==24){
            settingGetDataPart2State=BluetoothProtocol.SETTING_GET_DATA_PART2_OK;
            setting.handlerReceivedData(settingGetDataPart2State,data);
            //Log.i(TAG, "收到读取数据二！！！！" );
            return;
        }
        if(settingSetDataPart1State==BluetoothProtocol.SETTING_SET_DATA_PART1&&data.length==4){
            int[] aim_set_part1_ok={0x68,0x31,0x0D,0x0A};
            if(Arrays.equals(aim_set_part1_ok,data)){
                settingSetDataPart1State=BluetoothProtocol.SETTING_SET_DATA_PART1_OK;
                flag=true;
            }
            else {
                int[] aim_set_part1_failed={0x68,0x30,0x0D,0x0A};
                if(Arrays.equals(aim_set_part1_failed,data)){
                    settingSetDataPart1State=BluetoothProtocol.SETTING_SET_DATA_PART1_FAILED;
                    flag=true;
                }
            }
            //Log.i(TAG, "设置读取数据一成功！！！！" );
            if(flag){
                setting.handlerReceivedData(settingSetDataPart1State,new int[]{});
                return;
            }
        }
        if(settingSetDataPart2State==BluetoothProtocol.SETTING_SET_DATA_PART2&&data.length==4){
            int[] aim_set_part2_ok={0x68,0x31,0x0D,0x0A};
            if(Arrays.equals(aim_set_part2_ok,data)){
                settingSetDataPart2State=BluetoothProtocol.SETTING_SET_DATA_PART2_OK;
                flag=true;
            }
            else {
                int[] aim_set_part2_failed={0x68,0x30,0x0D,0x0A};
                if(Arrays.equals(aim_set_part2_failed,data)){
                    settingSetDataPart2State=BluetoothProtocol.SETTING_SET_DATA_PART2_FAILED;
                    flag=true;
                }
            }
           //Log.i(TAG, "设置读取数据二成功！！！！" );
            if(flag){
                setting.handlerReceivedData(settingSetDataPart2State,new int[]{});
                return;
            }
        }
    }

    /**
     * @description: SettingFragment处理收到的数据
     * @author: lyj
     * @create: 2019/11/27
     **/
    public void handleReceivedDataBySpectrumFragment(int[] data){
        boolean flag=false;
        SpectrumFragment spectrumFragment =
                (SpectrumFragment)getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
        if(receivedState==BluetoothProtocol.STOP_MEASURE){
            int[] aim_stop_measure_ok={0x68,0x31,0x0D,0x0A};
            if(Arrays.equals(aim_stop_measure_ok,data)){
                receivedState=BluetoothProtocol.NO_STATE;
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
                return;
            }
        }
        //获取能谱
        if(getSpectrum==BluetoothProtocol.GET_SPECTRUM&&data.length==2051){
            int[] nums=new int[(data.length-3)/2];
            for(int i=0;i<nums.length;i++){
                nums[i]=BluetoothProtocol.getVal(data,2*i+1,2*i+2);
            }
            MainActivity.getSpectrum=BluetoothProtocol.NO_STATE;
            Fragment currentFragment=getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
            if( currentFragment!=null&&currentFragment instanceof SpectrumFragment){
                SpectrumListener myListener=(SpectrumListener)spectrumFragment;
                myListener.notifyDataChanged(nums);
                Log.i(TAG, "-------------------发送谱数据------------");
            }
        }
        if(data.length==29){
            spectrumFragment.handlerReceivedData(receivedState,data);
            return;
        }
        /*if(MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS){
            int[] aim_shake_hands_ok={0x68,0x32,0x0D,0x0A};
            if(Arrays.equals(aim_shake_hands_ok,data)){
                receivedState=BluetoothProtocol.SHAKE_HANDS_OK;
                stopConnectTimer();
                flag=true;
            }
            else {
                int[] aim_shake_hands_failed={0x68,0x30,0x0D,0x0A};
                if(Arrays.equals(aim_shake_hands_failed,data)){
                    receivedState=BluetoothProtocol.SHAKE_HANDS_FAILED;
                    flag=true;
                }
            }
            *//*if(flag){
                spectrumFragment.handlerReceivedData(receivedState,new int[]{});
                return;
            }*//*
        }*/


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
        setAutoFlagToTrue();
        startActivity(intent);
    }

    /**
     * @description: autoFlag切换至true,切回巡测自动重连
     * @author: lyj
     * @create: 2019/11/27
     **/
    public static void setAutoFlagToTrue(){
        if(receivedState==BluetoothProtocol.SHAKE_HANDS_OK||receivedState==BluetoothProtocol.GET_DATA){
            Log.i(TAG, "-------------------receivedState is "+receivedState);
            autoFlag=true;
        }
    }

    public void handlerAlert(int[] data){
        if(!isAlertOpen){
            return;
        }
        alertState=BluetoothProtocol.ALERT_START;
        MonitorFragment monitor =
                (MonitorFragment)getSupportFragmentManager().findFragmentById(R.id.id_fragment_content);
        monitor.handlerReceivedData(BluetoothProtocol.ALERT_START,data);
        if(!MainActivity.isSendCloseAlert){
            Log.i(TAG, "-------------------自动关闭开始------------");
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    // 需要做的事:发送消息
                    Message message = new Message();
                    message.what = 1;
                    alertHandler.sendMessage(message);
                }
            };
            timer.schedule(task, MainActivity.closeAlertTime*1000);
            MainActivity.isSendCloseAlert=true;
            isNoAlert=false;
        }
    }

     Handler alertHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    if(isAlertOpen&&!isNoAlert){
                        Log.i(TAG, "----------------自动关闭报警----------------" );
                        stopTimer();
                        //startTimer(2);
                        startTimer(4);
                        isNoAlert=false;
                    }
                    MainActivity.isAlertOpen=false;
                    break;
            }
            super.handleMessage(msg);
        }
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
                    break;
                case 2:
                    //主动关
                    byte[] data={0x01};
                    MainActivity.send(BluetoothProtocol.ALERT_CLOSE,data);
                    break;
                case 3:
                    //Toast.makeText(getApplicationContext(), "开始4", Toast.LENGTH_LONG).show();
                    MainActivity.send(BluetoothProtocol.GET_DATA,new byte[]{});
                    break;
                case 4:
                    //自动关
                    byte[] data1={0x02};
                    MainActivity.send(BluetoothProtocol.ALERT_CLOSE,data1);
                    break;
                case 5:
                    unbindService(mServiceConnection);
                    unregisterReceiver(mGattUpdateReceiver);
                    mBluetoothLeService = null;
                    if(mBluetoothLeService==null){
                        Log.e(TAG, "-----------------null--------------");
                    }
                    else{
                        Log.e(TAG, "-----------------not null--------------");
                    }
                    break;
                case 6:
                    connect();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /*public void startTimer(int msg) {
        if(timer == null) {
            timer = new Timer();
        }
        task=null;
        switch (msg){
            case 2:
                task = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                };
                Log.i(TAG, "-------------------case2------------");
                timer.schedule(task, 500,1000);
                break;
            case 3:
                task = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                };
                Log.i(TAG, "-------------------case3------------");
                timer.schedule(task, 500,1000);
                break;
        }
        Log.i(TAG, "-------------------start  timer------------");
    }

    public void stopTimer(){
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        if(task != null) {
            task.cancel();
            task = null;
        }
        Log.i(TAG, "-------------------stopTimer------------");
        *//*if(timer==null){
            Log.i(TAG, "-------------------stopTimer  null-----------");
        }
        if(timer != null) {
            timer.cancel();
            timer = null;
            Log.i(TAG, "-------------------stopTimer------------");
        }

        if(task==null){
            Log.i(TAG, "-------------------stopTask  null-----------");
        }
        if(task != null) {
            task.cancel();
            task = null;
            Log.i(TAG, "-------------------stop Task------------");
        }*//*
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
                    break;
                case 2:
                    MainActivity.send(BluetoothProtocol.ALERT_CLOSE,new byte[]{});
                    break;
                case 3:
                    Log.i(TAG, "-------------------received------------");
                    MainActivity.send(BluetoothProtocol.GET_DATA,new byte[]{});
                    break;
            }
            super.handleMessage(msg);
        }
    };*/


    public void startTimer(int msg) {
        if(connectTimer == null) {
            connectTimer = new Timer();
        }
        connectTask=null;
        switch(msg){
            case 1:
                connectTask = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                };
                connectTimer.schedule(connectTask, 100,1000);
                break;
            case 2:
                connectTask = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                };
                Log.i(TAG, "-------------------case2------------");
                connectTimer.schedule(connectTask, 100,1000);
                break;
            case 3:
                connectTask = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                };
                //Toast.makeText(getApplicationContext(), "开始3", Toast.LENGTH_LONG).show();
                Log.i(TAG, "-------------------case3------------");
                connectTimer.schedule(connectTask, 100,1000);
                break;
            case 4:
                connectTask = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 4;
                        handler.sendMessage(message);
                    }
                };
                Log.i(TAG, "-------------------case4------------");
                connectTimer.schedule(connectTask, 100,1000);
                break;
            case 5:
                connectTask = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 5;
                        handler.sendMessage(message);
                    }
                };
                Log.i(TAG, "-------------------case5-----------");
                connectTimer.schedule(connectTask, 100);
                break;
            case 6:
                connectTask = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 6;
                        handler.sendMessage(message);
                    }
                };
                Log.i(TAG, "-------------------case6-----------");
                connectTimer.schedule(connectTask, 500);
                break;

        }
    }

    public void stopTimer(){
        if(connectTimer != null) {
            connectTimer.cancel();
            connectTimer = null;
        }

        if(connectTask != null) {
            connectTask.cancel();
            connectTask = null;
        }
    }

    public void stopAlertTimer(){
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        if(task != null) {
            task.cancel();
            task = null;
        }
    }

    public void changeBluetooth(String address){
        disconnect();
        connectedAddress=address;
        startTimer(6);
        Log.e(TAG, "-----------------click 2--------------");
        /*if(connectFlag){
            //Log.e(TAG, "-----------------connect 1--------------");
            mBluetoothLeService.disconnect();
            secondConnect=true;
            connectedAddress=address;
        }
        else{
            //Log.e(TAG, "-----------------connect 2--------------");
            mBluetoothLeService.disconnect();
            connectedAddress=address;
            mBluetoothLeService.connect(address);
        }*/
        //Log.e(TAG, "-----------------address is--------------"+address);
    }

    public void disconnect(){
        /*if(mBluetoothLeService==null){
            Log.e(TAG, "-----------------really null--------------");
        }*/
        if(mBluetoothLeService!=null){
            mBluetoothLeService.disconnect();
            startTimer(5);
        }
        /*if(connectFlag){
            mBluetoothLeService.disconnect();
        }
        else{

        }*/
    }
    public void connect(){
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }
    public void testSave(){
        DataTotalMsg msg=new DataTotalMsg();
        msg.setStartTime("2019-12-22 01:24:05");
        msg.setIsAlarm("否");
        msg.save();

        DataTotalMsg msg1=new DataTotalMsg();
        msg1.setStartTime("2019-12-23 01:24:05");
        msg1.setIsAlarm("否");
        msg1.save();

    }
}

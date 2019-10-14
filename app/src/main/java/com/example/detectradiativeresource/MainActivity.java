package com.example.detectradiativeresource;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.detectradiativeresource.bluetooth.BluetoothFragment;
import com.example.detectradiativeresource.bluetooth.library.BluetoothSPP;
import com.example.detectradiativeresource.bluetooth.library.BluetoothState;
import com.example.detectradiativeresource.bluetooth.library.DeviceList;
import com.example.detectradiativeresource.data.DataFragment;
import com.example.detectradiativeresource.log.LogFragment;
import com.example.detectradiativeresource.monitor.MonitorFragment;
import com.example.detectradiativeresource.monitor.trace.LocationService;
import com.example.detectradiativeresource.setting.SettingFragment;
import com.orm.SugarContext;

public class MainActivity extends AppCompatActivity {
    /**
     * 底部导航栏的widdget
     */
    public static final String TAG="MainActivity";
    private RadioGroup mNavGroup;
    private TextView bluetoothMsg;
    private FragmentTransaction mTransaction;
    public static LocationService locationService;
    private FrameLayout mFlLifeRoot;
    public static BluetoothSPP bt;
    public static double longitude=0.0;
    public static double latitude=0.0;
    public static int AlarmVal=1400;
    public static int SeriousVal=1800;
    public static int errorVal=2200;
    /**
     * 五个Fragments
     */
    Fragment monitorFragemnt, bluetoothFragment, dataFragment,settingFragment, logFragment;
    public static final int VIEW_MONITOR_INDEX = 0;
    public static final int VIEW_BLUETOOTH_INDEX = 1;
    public static final int VIEW_DATA_INDEX = 2;
    public static final int VIEW_SETTING_INDEX = 3;
    public static final int VIEW_LOG_INDEX = 4;
    private int temp_position_index = -1;
    public static String IP= "http://10.28.224.50:8000/";//服务器IP地址以及端口

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationService = new LocationService(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();

        SugarContext.init(this);
        /**************************************蓝牙配置页面*************************************/
        bt = new BluetoothSPP(this);
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {

            }
        });
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "连接到" + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
                Log.i(TAG, "-------------- "+bluetoothMsg.getText());
                BluetoothListener myListener=(BluetoothListener)bluetoothFragment;
                myListener.setText("连接成功");
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "连接断开", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "-------------- "+bluetoothMsg.getText());
                BluetoothListener myListener=(BluetoothListener)bluetoothFragment;
                myListener.setText("连接失败");
            }
            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "连接不可用", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
        LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.fragment_bluetooth, null);
        bluetoothMsg=view.findViewById(R.id.id_bluetooth_msg);

        mNavGroup = findViewById(R.id.id_navcontent);
        monitorFragemnt = MonitorFragment.getNewInstance();
        bluetoothFragment = BluetoothFragment.getNewInstance();
        dataFragment = DataFragment.getNewInstance();
        settingFragment = SettingFragment.getNewInstance();
        logFragment = LogFragment.getNewInstance();
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
                if (temp_position_index != VIEW_BLUETOOTH_INDEX) {
                    //显示
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mFlLifeRoot.removeAllViews();
                    mTransaction.replace(R.id.id_fragment_content, bluetoothFragment);
                    mTransaction.commit();
                }
                temp_position_index = VIEW_BLUETOOTH_INDEX;
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    showbluetoothDialog();
                    //bt.disconnect();
                } else {
                    //显示
                        /*mTransaction = getSupportFragmentManager().beginTransaction();
                        mFlLifeRoot.removeAllViews();
                        mTransaction.replace(R.id.id_fragment_content, bluetoothFragment);
                        mTransaction.commit();*/
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
                break;
            case R.id.id_nav_bt_data:
                if (temp_position_index != VIEW_DATA_INDEX) {
                    //显示
                    mTransaction = getSupportFragmentManager().beginTransaction();
                    mFlLifeRoot.removeAllViews();
                    mTransaction.replace(R.id.id_fragment_content, dataFragment);
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
        //检查蓝牙是否可用
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK){
                bt.connect(data);
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "蓝牙不可用"
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
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
                        bt.disconnect();
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

    public interface BluetoothListener{
        public void setText(String code);
    }
}

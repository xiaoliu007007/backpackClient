package com.example.detectradiativeresource.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.data.DataMsgAdapter;
import com.example.detectradiativeresource.utils.DataHelperUtils;
import com.example.detectradiativeresource.utils.FragmentChangeUtils;

public class BluetoothFragment extends Fragment implements MainActivity.BluetoothListener{
    private final static  String TAG="BluetoothFragment";
    // 缓存Fragment view
    private View rootView;
    private static BluetoothFragment bluetoothFragment;
    private TextView bluetoothMsg;
    private BluetoothAdapter mAdapter;
    private ListView infoList;
    private Button searchBtn;
    private Button disconnectBtn;
    private android.bluetooth.BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 3000;
    private MainActivity activity;
    public BluetoothFragment(){}
    public static BluetoothFragment getNewInstance(){
        if (bluetoothFragment ==null){
            bluetoothFragment =new BluetoothFragment();
        }
        return bluetoothFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mHandler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        activity=(MainActivity)getActivity();
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_bluetooth,container, false);
        init(rootView);
        /*bluetoothMsg=rootView.findViewById(R.id.id_bluetooth_msg);
        bluetoothMsg.setText(MainActivity.connectedStateMsg);*/
        return rootView;
    }
    private void init(View view){
        infoList = (ListView) view.findViewById(R.id.bluetooth_list);
        mAdapter = new BluetoothAdapter(getActivity());
        infoList.setAdapter(mAdapter);
        scanLeDevice(true);
        infoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "-----------------click 1--------------");
                final BluetoothDevice device = mAdapter.getDevice(i);
                if (device == null) {
                    return;
                }
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                activity.changeBluetooth(device.getAddress());
            }
        });
        searchBtn = (Button)view.findViewById(R.id.bluetooth_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                scanLeDevice(true);
            }
        });
        disconnectBtn=(Button)view.findViewById(R.id.bluetooth_disconnect);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.disconnect();
            }
        });

    }
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private android.bluetooth.BluetoothAdapter.LeScanCallback mLeScanCallback =
            new android.bluetooth.BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    mAdapter.addDevice(bluetoothDevice);
                    mAdapter.notifyDataSetChanged();
                }
            };
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void change() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
        scanLeDevice(true);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    /*@Override
    public void setText(String msg) {
        bluetoothMsg.setText(msg);
    }*/

}


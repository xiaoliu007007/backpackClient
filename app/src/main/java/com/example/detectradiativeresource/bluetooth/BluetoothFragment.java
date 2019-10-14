package com.example.detectradiativeresource.bluetooth;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.R;

public class BluetoothFragment extends Fragment implements MainActivity.BluetoothListener {
    // 缓存Fragment view
    private View rootView;
    private static BluetoothFragment bluetoothFragment;
    private TextView bluetoothMsg;
    public BluetoothFragment(){}
    public static BluetoothFragment getNewInstance(){
        if (bluetoothFragment ==null){
            bluetoothFragment =new BluetoothFragment();
        }
        return bluetoothFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_bluetooth,container, false);
        bluetoothMsg=rootView.findViewById(R.id.id_bluetooth_msg);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setText(String msg) {
        bluetoothMsg.setText(msg);
    }
}


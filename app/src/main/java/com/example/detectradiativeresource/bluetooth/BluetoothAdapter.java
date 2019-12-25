package com.example.detectradiativeresource.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.bluetooth.library.DeviceScanActivity;
import com.example.detectradiativeresource.dao.DataMsg;
import com.example.detectradiativeresource.utils.BluetoothProtocol;

import java.util.ArrayList;
import java.util.List;

public class BluetoothAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private List<BluetoothDevice> mLeDevices;
    public BluetoothAdapter(Context context){
        mLeDevices = new ArrayList<BluetoothDevice>();
        mLayoutInflater = LayoutInflater.from(context);
    }
    public void addDevice(BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(view == null)
        {
            viewHolder = new ViewHolder();
            view = mLayoutInflater.inflate(R.layout.bluetooth_item, null);
            //实例化控件
            viewHolder.deviceName=(TextView) view.findViewById(R.id.bluetooth_name);
            viewHolder.deviceAddress= (TextView) view.findViewById(R.id.bluetooth_address);
            viewHolder.deviceMsg = (TextView) view.findViewById(R.id.bluetooth_connect_msg);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        BluetoothDevice device = mLeDevices.get(i);
        //设置控件数据
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());
        if(MainActivity.connectFlag&&MainActivity.connectedAddress!=null&&MainActivity.connectedAddress.length()>0&&MainActivity.connectedAddress.equals(device.getAddress())){
            viewHolder.deviceMsg.setText("已连接");
        }
        else{
            viewHolder.deviceMsg.setText("未连接");
        }
        return view;
    }
    class ViewHolder{
        public TextView deviceName;
        public TextView deviceAddress;
        public TextView deviceMsg;
    }
}

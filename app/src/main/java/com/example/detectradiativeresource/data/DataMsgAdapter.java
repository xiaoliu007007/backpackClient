package com.example.detectradiativeresource.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.dao.DataMsg;

import java.util.List;

/**
 * @description: 上传Adatper
 * @author: lyj
 * @create: 2019/09/02
 **/
public class DataMsgAdapter extends BaseAdapter {
    private Context ctx;
    private LayoutInflater mLayoutInflater;
    private List<DataMsg> list;
    public DataMsgAdapter(Context context, List<DataMsg> list) {
        mLayoutInflater = LayoutInflater.from(context);
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        return list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.datamsg_item, null);
            //实例化控件
            viewHolder.viewId=(TextView) convertView.findViewById(R.id.item_id);
            viewHolder.time= (TextView) convertView.findViewById(R.id.item_time);
            viewHolder.value = (TextView) convertView.findViewById(R.id.item_value);
            viewHolder.longitude = (TextView) convertView.findViewById(R.id.item_longitude);
            viewHolder.latitude = (TextView) convertView.findViewById(R.id.item_latitude);
            viewHolder.isAlarm=(TextView) convertView.findViewById(R.id.item_isAlarm);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //抽取bean对象
        DataMsg msg = list.get(position);
        //设置控件数据
        viewHolder.viewId.setText(String.valueOf(position+1));
        viewHolder.time.setText(msg.getTime());
        viewHolder.value.setText(msg.getNaI_jishu()+"CPS");
        viewHolder.longitude.setText(String.valueOf(msg.getLongitude()));
        viewHolder.latitude.setText(String.valueOf(msg.getLatitude()));
        viewHolder.isAlarm.setText(msg.getIsAlarm());
        return convertView;
    }
    class ViewHolder{
        public TextView viewId;
        public TextView time;
        public TextView value;
        public TextView longitude;
        public TextView latitude;
        public TextView isAlarm;
    }
}

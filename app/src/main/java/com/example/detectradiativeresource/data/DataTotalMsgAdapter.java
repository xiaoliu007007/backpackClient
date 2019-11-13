package com.example.detectradiativeresource.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.dao.DataMsg;
import com.example.detectradiativeresource.dao.DataTotalMsg;

import java.util.List;

/**
 * @description: 上传Adatper
 * @author: lyj
 * @create: 2019/09/02
 **/
public class DataTotalMsgAdapter extends BaseAdapter {
    private Context ctx;
    private LayoutInflater mLayoutInflater;
    private List<DataTotalMsg> list;
    public DataTotalMsgAdapter(Context context, List<DataTotalMsg> list) {
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
            convertView = mLayoutInflater.inflate(R.layout.datatotalmsg_item, null);
            //实例化控件
            viewHolder.viewId=(TextView) convertView.findViewById(R.id.item_total_id);
            viewHolder.start_time= (TextView) convertView.findViewById(R.id.item_total_start_time);
            viewHolder.end_time = (TextView) convertView.findViewById(R.id.item_total_end_time);
            viewHolder.isAlarm=(TextView) convertView.findViewById(R.id.item_total_isAlarm);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //抽取bean对象
        DataTotalMsg msg = list.get(position);
        //设置控件数据
        viewHolder.viewId.setText(String.valueOf(msg.getId()));
        viewHolder.start_time.setText(msg.getStartTime());
        viewHolder.end_time.setText(msg.getEndTime()==null?"-":msg.getEndTime());
        viewHolder.isAlarm.setText(msg.getIsAlarm());
        return convertView;
    }
    class ViewHolder{
        public TextView viewId;
        public TextView start_time;
        public TextView end_time;
        public TextView isAlarm;
    }
}

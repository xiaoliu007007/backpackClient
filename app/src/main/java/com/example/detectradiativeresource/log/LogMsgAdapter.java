package com.example.detectradiativeresource.log;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.dao.LogMsg;

import java.util.List;

/**
 * @description: LogAdatper
 * @author: lyj
 * @create: 2019/10/15
 **/
public class LogMsgAdapter extends BaseAdapter {
    private Context ctx;
    private LayoutInflater mLayoutInflater;
    private List<LogMsg> list;
    public LogMsgAdapter(Context context, List<LogMsg> list) {
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
            convertView = mLayoutInflater.inflate(R.layout.logmsg_item, null);
            //实例化控件
            viewHolder.id= (TextView) convertView.findViewById(R.id.item_log_id);
            viewHolder.type = (TextView) convertView.findViewById(R.id.item_log_type);
            viewHolder.startTime= (TextView) convertView.findViewById(R.id.item_log_start_time);
            viewHolder.endTime = (TextView) convertView.findViewById(R.id.item_log_end_time);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //抽取bean对象
        LogMsg msg = list.get(position);
        //设置控件数据
        viewHolder.id.setText(String.valueOf(position+1));
        viewHolder.type.setText(msg.getType());
        viewHolder.startTime.setText(msg.getStartTime());
        viewHolder.endTime.setText(msg.getEndTime());
        return convertView;
    }
    class ViewHolder{
        public TextView id;
        public TextView type;
        public TextView startTime;
        public TextView endTime;
    }
}

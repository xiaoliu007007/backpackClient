package com.example.detectradiativeresource.log;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.dao.LogDetailMsg;
import com.example.detectradiativeresource.dao.LogMsg;

import java.util.List;

/**
 * @description: LogDetailMsgAdapter
 * @author: lyj
 * @create: 2019/11/13
 **/
public class LogDetailMsgAdapter extends BaseAdapter {
    private Context ctx;
    private LayoutInflater mLayoutInflater;
    private List<LogDetailMsg> list;
    public LogDetailMsgAdapter(Context context, List<LogDetailMsg> list) {
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
            convertView = mLayoutInflater.inflate(R.layout.logdetailmsg_item, null);
            //实例化控件
            viewHolder.id= (TextView) convertView.findViewById(R.id.item_log_detail_id);
            viewHolder.content = (TextView) convertView.findViewById(R.id.item_log_detail_content);
            viewHolder.time= (TextView) convertView.findViewById(R.id.item_log_detail_time);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //抽取bean对象
        LogDetailMsg msg = list.get(position);
        //设置控件数据
        viewHolder.id.setText(String.valueOf(position+1));
        viewHolder.content.setText(msg.getContent());
        viewHolder.time.setText(msg.getTime());
        return convertView;
    }
    class ViewHolder{
        public TextView id;
        public TextView content;
        public TextView time;
    }
}

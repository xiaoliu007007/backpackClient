package com.example.detectradiativeresource.log;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.dao.LogDetailMsg;
import com.example.detectradiativeresource.dao.LogMsg;
import com.example.detectradiativeresource.utils.DataHelperUtils;
import com.example.detectradiativeresource.utils.FragmentChangeUtils;

import java.util.List;

/**
 * @description: 日志二级列表
 * @author: lyj
 * @create: 2019/11/13
 **/
public class LogDetailFragment extends Fragment{
    // 缓存Fragment view
    LogFragmentChangeListener myListener;
    private View rootView;
    private static LogDetailFragment logFragment;
    private ListView infoList;
    private LogDetailMsgAdapter mAdapter;
    private List<LogDetailMsg> mdata;

    public LogDetailFragment(){}
    public static LogDetailFragment getNewInstance(){
        if (logFragment ==null){
            logFragment =new LogDetailFragment();
        }
        return logFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_logdetail, container, false);
        init(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void init(View view){
        infoList = (ListView) view.findViewById(R.id.log_detail_list);
        mdata = DataHelperUtils.findLogDetailMsgByParent(FragmentChangeUtils.logMsgId);
        mAdapter = new LogDetailMsgAdapter(getActivity(), mdata);
        infoList.setAdapter(mAdapter);
        Button reButton = (Button)view.findViewById(R.id.log_return);
        reButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentChangeUtils.isLogFragment=true;
                myListener.changeLogDetailFragment();
            }
        });
        infoList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "删除");

            }
        });
    }
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int position=info.position;
        switch (item.getItemId()) {
            case 0:
                long deleteId= DataHelperUtils.findLogDetailMsgId(FragmentChangeUtils.logMsgId,position);
                if(deleteId>0){
                    DataHelperUtils.deleteLogDetailMsgId(deleteId);
                }
                init(rootView);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myListener=(LogFragmentChangeListener)activity;
    }

    public interface LogFragmentChangeListener{
        public void changeLogDetailFragment();
    }

}


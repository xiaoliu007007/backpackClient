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
import android.widget.ListView;

import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.dao.LogMsg;
import com.example.detectradiativeresource.utils.DataHelperUtils;
import com.example.detectradiativeresource.utils.FragmentChangeUtils;

import java.util.List;

public class LogFragment extends Fragment{
    // 缓存Fragment view
    LogDetailFragmentChangeListener myListener;
    private View rootView;
    private static LogFragment logFragment;
    private ListView infoList;
    private LogMsgAdapter mAdapter;
    private List<LogMsg> mdata;

    public LogFragment(){}
    public static LogFragment getNewInstance(){
        if (logFragment ==null){
            logFragment =new LogFragment();
        }
        return logFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_log, container, false);
        init(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void init(View view){
        infoList = (ListView) view.findViewById(R.id.log_list);
        mdata = DataHelperUtils.findAllLogMsg();
        mAdapter = new LogMsgAdapter(getActivity(), mdata);
        infoList.setAdapter(mAdapter);
        infoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                long id= DataHelperUtils.findLogMsgId(i);
                if(id>0){
                    FragmentChangeUtils.logMsgId=id;
                    FragmentChangeUtils.isLogFragment=false;
                    myListener.changeLogDetailFragment();
                }
            }
        });
        infoList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "查看");
                menu.add(0, 1, 0, "删除");

            }
        });
    }
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int position=info.position;
        switch (item.getItemId()) {
            case 0:
                long id= DataHelperUtils.findLogMsgId(position);
                if(id>0){
                    FragmentChangeUtils.logMsgId=id;
                    FragmentChangeUtils.isLogFragment=false;
                    myListener.changeLogDetailFragment();
                }
                break;
            case 1:
                long deleteId= DataHelperUtils.findLogMsgId(position);
                if(deleteId>0){
                    DataHelperUtils.deleteLogMsgById(deleteId);
                }
                init(rootView);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myListener=(LogDetailFragmentChangeListener)activity;
    }

    public interface LogDetailFragmentChangeListener{
        public void changeLogDetailFragment();
    }

}


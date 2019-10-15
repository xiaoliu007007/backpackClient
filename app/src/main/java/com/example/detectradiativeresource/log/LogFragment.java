package com.example.detectradiativeresource.log;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.dao.LogMsg;

import java.util.List;

public class LogFragment extends Fragment{
    // 缓存Fragment view
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
        infoList = (ListView) view.findViewById(R.id.loglist);
        mdata = find();
        mAdapter = new LogMsgAdapter(getActivity(), mdata);
        infoList.setAdapter(mAdapter);
    }
    public List<LogMsg> find(){
        return LogMsg.listAll(LogMsg.class);
    }
}


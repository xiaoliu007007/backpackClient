package com.example.detectradiativeresource.log;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.detectradiativeresource.R;

public class LogFragment extends Fragment{
    // 缓存Fragment view
    private View rootView;
    private static LogFragment logFragment;
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
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}


package com.example.detectradiativeresource.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.detectradiativeresource.R;

public class SettingFragment extends Fragment{
    // 缓存Fragment view
    private View rootView;
    private static SettingFragment settingFragment;
    public SettingFragment(){}
    public static SettingFragment getNewInstance(){
        if (settingFragment ==null){
            settingFragment =new SettingFragment();
        }
        return settingFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}


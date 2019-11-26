package com.example.detectradiativeresource.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.utils.DataHelperUtils;

public class SettingFragment extends Fragment{
    // 缓存Fragment view
    private static final String TAG = "SettingFragment";
    private View rootView;
    private static SettingFragment settingFragment;
    private int alertVal;//报警时间
    private int timeVal; //能谱采集时间
    private int forceTimeVal;//强制本底采集时间
    private int alertFlag;//声光报警 0开1关
    private int collectType;//测量单位，0计数率，1计量率
    private int thresholdType=1; //1,2,3NaI,GM,中子
    private int NaIAVal;
    private int NaIBVal;
    private int NaICVal;
    private int NaIDVal;
    private int thresholdHigh;//高本底阈值
    private int thresholdLow;//低本底阈值
    private int thresholdAlert;//报警阈值

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
        init(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void init(View view){
        final EditText alert =(EditText) view.findViewById (R.id.alert_val);
        final EditText time =(EditText) view.findViewById (R.id.collect_time);
        final EditText forceTime =(EditText) view.findViewById (R.id.collect_force_time);
        final EditText threshold_high_val =(EditText) view.findViewById (R.id.threshold_high_val);
        final EditText threshold_low_val =(EditText) view.findViewById (R.id.threshold_low_val);
        final EditText NaI_a =(EditText) view.findViewById (R.id.NaI_val_a);
        final EditText NaI_b =(EditText) view.findViewById (R.id.NaI_val_b);
        final EditText NaI_c =(EditText) view.findViewById (R.id.NaI_val_c);
        final EditText NaI_d =(EditText) view.findViewById (R.id.NaI_val_d);
        final EditText alert_threshold_val =(EditText) view.findViewById (R.id.alert_threshold_val);
        final RadioGroup alert_flag_rg=(RadioGroup)view.findViewById(R.id.alert_flag_rg);
        final RadioGroup collect_type=(RadioGroup)view.findViewById(R.id.collect_type);
        final RadioGroup thresholdTyperg=(RadioGroup)view.findViewById(R.id.threshold_type);
        alert_flag_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.alert_flag_open:
                        alertFlag=0;
                        break;
                    case R.id.alert_flag_close:
                        alertFlag=1;
                        break;
                }
            }
        });
        collect_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.collect_shu:
                        collectType=0;
                        break;
                    case R.id.collect_liang:
                        collectType=1;
                        break;
                }
            }
        });
        thresholdTyperg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.threshold_nai:
                        thresholdType=1;
                        break;
                    case R.id.threshold_gm:
                        thresholdType=2;
                        break;
                    case R.id.threshold_zz:
                        thresholdType=3;
                        break;
                }
            }
        });

        Button setting = (Button)view.findViewById(R.id.button_setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ans="";
                if(alert.getText().toString().equals("")){
                    ans+="没有设置报警时间";
                }
                else{
                    alertVal=Integer.parseInt(alert.getText().toString());
                    ans+="报警时间设置为"+alertVal;
                }
                if(time.getText().toString().equals("")){
                    ans+="没有设置能谱采集时间";
                }
                else{
                    timeVal=Integer.parseInt(time.getText().toString());
                    ans+="能谱采集时间设置为"+timeVal;
                }
                if(forceTime.getText().toString().equals("")){
                    ans+="没有设置强制本底采集时间";
                }
                else{
                    forceTimeVal=Integer.parseInt(forceTime.getText().toString());
                    ans+="强制本底时间设置为"+forceTimeVal;
                }
                ans+="声光报警类型为:"+alertFlag;
                ans+="测量单位类型为:"+collectType;
                ans+="---类型为:"+thresholdType;

                if(threshold_high_val.getText().toString().equals("")||threshold_low_val.getText().toString().equals("")){
                    ans+="高低本底阈值未都设置";
                }
                else{
                    thresholdHigh=Integer.parseInt(threshold_high_val.getText().toString());
                    thresholdLow=Integer.parseInt(threshold_low_val.getText().toString());
                    ans+="高本底阈值为"+thresholdHigh+"低本底阈值为"+thresholdLow;
                }
                if(NaI_a.getText().toString().equals("")||NaI_b.getText().toString().equals("")||NaI_c.getText().toString().equals("")||NaI_d.getText().toString().equals("")){
                    ans+="A,B,C,D四个参数未都设置";
                }
                else{
                    NaIAVal=Integer.parseInt(NaI_a.getText().toString());
                    NaIBVal=Integer.parseInt(NaI_b.getText().toString());
                    NaICVal=Integer.parseInt(NaI_c.getText().toString());
                    NaIDVal=Integer.parseInt(NaI_d.getText().toString());
                    ans+="A："+NaIAVal;
                    ans+="B："+NaIBVal;
                    ans+="C："+NaICVal;
                    ans+="D："+NaIDVal;
                }
                if(alert_threshold_val.getText().toString().equals("")){
                    ans+="没有设置报警时间";
                }
                else{
                    thresholdAlert=Integer.parseInt(alert_threshold_val.getText().toString());
                    ans+="报警时间设置为"+thresholdAlert;
                }
                Toast.makeText(getActivity().getApplicationContext(), ans, Toast.LENGTH_LONG).show();
            }
        });
    }
}


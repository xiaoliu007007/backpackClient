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

import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.utils.BluetoothProtocol;
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
    private int thresholdAlert_1;//报警阈值
    private int thresholdAlert_2;
    private int thresholdAlert_3;
    private int thresholdAlert_4;
    private int thresholdAlert_5;
    private boolean thresholdHLFlag=false;//高低本底阈值标志
    private boolean amendFlag=false;//NaI修正标志
    private boolean collectTimeFlag=false;//采集时间标志
    private boolean collectBGFlag=false;//强制采集本底标志
    private boolean thresholdAlertFlag=false;//报警阈值标志

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
        final EditText alert_threshold_val_1 =(EditText) view.findViewById (R.id.alert_threshold_val_1);
        final EditText alert_threshold_val_2 =(EditText) view.findViewById (R.id.alert_threshold_val_2);
        final EditText alert_threshold_val_3 =(EditText) view.findViewById (R.id.alert_threshold_val_3);
        final EditText alert_threshold_val_4 =(EditText) view.findViewById (R.id.alert_threshold_val_4);
        final EditText alert_threshold_val_5 =(EditText) view.findViewById (R.id.alert_threshold_val_5);
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
                    //ans+="没有设置报警时间";
                }
                else{
                    alertVal=Integer.parseInt(alert.getText().toString());
                    collectTimeFlag=true;
                    //ans+="报警时间设置为"+alertVal;
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
                if(alert_threshold_val_1.getText().toString().equals("")||alert_threshold_val_2.getText().toString().equals("")
                ||alert_threshold_val_3.getText().toString().equals("")||alert_threshold_val_4.getText().toString().equals("")||alert_threshold_val_5.getText().toString().equals("")){
                    ans+="没有设置报警阈值";
                }
                else{
                    thresholdAlert_1=Integer.parseInt(alert_threshold_val_1.getText().toString());
                    thresholdAlert_2=Integer.parseInt(alert_threshold_val_2.getText().toString());
                    thresholdAlert_3=Integer.parseInt(alert_threshold_val_3.getText().toString());
                    thresholdAlert_4=Integer.parseInt(alert_threshold_val_4.getText().toString());
                    thresholdAlert_5=Integer.parseInt(alert_threshold_val_5.getText().toString());
                    ans+="报警阈值一设置为"+thresholdAlert_1;
                    ans+="报警阈值二设置为"+thresholdAlert_2;
                    ans+="报警阈值三设置为"+thresholdAlert_3;
                    ans+="报警阈值四设置为"+thresholdAlert_4;
                    ans+="报警阈值五设置为"+thresholdAlert_5;
                }
                //Toast.makeText(getActivity().getApplicationContext(), ans, Toast.LENGTH_LONG).show();
                if(MainActivity.receivedState== BluetoothProtocol.SHAKE_HANDS_FAILED||MainActivity.receivedState==BluetoothProtocol.NO_STATE){
                    MainActivity.send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
                }
                else{
                    sendSetting();
                }
            }
        });
    }
    /**
     * @description: 发送数据
     * @author: lyj
     * @create: 2019/11/27
     **/
    private void sendSetting(){
        if(thresholdHLFlag){

        }
        else if(amendFlag){

        }
        else if(collectTimeFlag){
            MainActivity.send(BluetoothProtocol.SETTING_COLLECT_TIME,new byte[]{(byte) alertVal});
        }
        else if(collectBGFlag){

        }
        else if(thresholdAlertFlag){

        }
    }

    /**
     * @description: 处理接受数据
     * @author: lyj
     * @create: 2019/11/27
     **/
    public void handlerReceivedData(String Type,int[] data){
        switch (Type) {
            case BluetoothProtocol.SHAKE_HANDS_OK:
                sendSetting();
                break;
            case BluetoothProtocol.SHAKE_HANDS_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "握手失败,无法设定", Toast.LENGTH_LONG).show();
                break;
            case BluetoothProtocol.SETTING_COLLECT_TIME_OK:
                Toast.makeText(getActivity().getApplicationContext(), "设置采集时间成功", Toast.LENGTH_LONG).show();
                collectTimeFlag=false;
                break;
            case BluetoothProtocol.SETTING_COLLECT_TIME_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "设置采集时间失败，请重新设置", Toast.LENGTH_LONG).show();
                collectTimeFlag=false;
                break;
        }
    }
}


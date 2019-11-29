package com.example.detectradiativeresource.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
    private EditText alert;
    private int timeVal; //能谱采集时间
    private EditText time;
    private int forceTimeVal;//强制本底采集时间
    private EditText forceTime;
    private int alertFlag;//声光报警 0开1关
    private int collectType;//测量单位，0计数率，1计量率
    private int thresholdType=1; //1,2,3NaI,GM,中子
    private int NaIAVal;
    private EditText NaI_a;
    private int NaIBVal;
    private EditText NaI_b;
    private int NaICVal;
    private EditText NaI_c;
    private int NaIDVal;
    private EditText NaI_d;
    private int thresholdHigh;//高本底阈值
    private EditText threshold_high_val;
    private int thresholdLow;//低本底阈值
    private EditText threshold_low_val;
    private int thresholdAlert_1;//报警阈值
    private EditText alert_threshold_val_1;
    private int thresholdAlert_2;
    private EditText alert_threshold_val_2;
    private int thresholdAlert_3;
    private EditText alert_threshold_val_3;
    private int thresholdAlert_4;
    private EditText alert_threshold_val_4;
    private int thresholdAlert_5;
    private EditText alert_threshold_val_5;
    private boolean thresholdHLFlag=false;//高低本底阈值标志
    private boolean amendFlag=false;//NaI修正标志
    private boolean collectTimeFlag=false;//采集时间标志
    private boolean collectBGFlag=false;//强制采集本底标志
    private boolean thresholdAlertFlag=false;//报警阈值标志
    private boolean isReading=false;//是否是读取中的状态

    private Button setting;
    private Button reading;

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
        alert =(EditText) view.findViewById (R.id.alert_val);
        time =(EditText) view.findViewById (R.id.collect_time);
        forceTime =(EditText) view.findViewById (R.id.collect_force_time);
        threshold_high_val =(EditText) view.findViewById (R.id.threshold_high_val);
        threshold_low_val =(EditText) view.findViewById (R.id.threshold_low_val);
        NaI_a =(EditText) view.findViewById (R.id.NaI_val_a);
        NaI_b =(EditText) view.findViewById (R.id.NaI_val_b);
        NaI_c =(EditText) view.findViewById (R.id.NaI_val_c);
        NaI_d =(EditText) view.findViewById (R.id.NaI_val_d);
        alert_threshold_val_1 =(EditText) view.findViewById (R.id.alert_threshold_val_1);
        alert_threshold_val_2 =(EditText) view.findViewById (R.id.alert_threshold_val_2);
        alert_threshold_val_3 =(EditText) view.findViewById (R.id.alert_threshold_val_3);
        alert_threshold_val_4 =(EditText) view.findViewById (R.id.alert_threshold_val_4);
        alert_threshold_val_5 =(EditText) view.findViewById (R.id.alert_threshold_val_5);
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
        reading=(Button)view.findViewById(R.id.button_setting_read);
        reading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.receivedState== BluetoothProtocol.SHAKE_HANDS_FAILED||MainActivity.receivedState==BluetoothProtocol.NO_STATE){
                    MainActivity.send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
                    isReading=true;
                }
                else{
                    sendReading();
                }
            }
        });
        setting = (Button)view.findViewById(R.id.button_setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ans="";
                if(alert.getText().toString().equals("")){
                    //ans+="没有设置报警时间";
                }
                else{
                    alertVal=Integer.parseInt(alert.getText().toString());
                    //ans+="报警时间设置为"+alertVal;
                }
                if(time.getText().toString().equals("")){
                    ans+="没有设置能谱采集时间";
                }
                else{
                    timeVal=Integer.parseInt(time.getText().toString());
                    ans+="能谱采集时间设置为"+timeVal;
                    collectTimeFlag=true;
                }
                if(forceTime.getText().toString().equals("")){
                    ans+="没有设置强制本底采集时间";
                }
                else{
                    forceTimeVal=Integer.parseInt(forceTime.getText().toString());
                    ans+="强制本底时间设置为"+forceTimeVal;
                    collectBGFlag=true;
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
                    thresholdHLFlag=true;
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
                    amendFlag=true;
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
                    thresholdAlertFlag=true;
                }
                //Toast.makeText(getActivity().getApplicationContext(), ans, Toast.LENGTH_LONG).show();
                if(MainActivity.receivedState== BluetoothProtocol.SHAKE_HANDS_FAILED||MainActivity.receivedState==BluetoothProtocol.NO_STATE){
                    MainActivity.send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
                    isReading=false;
                }
                else{
                    sendSetting();
                }
            }
        });
    }
    /**
     * @description: 读取数据
     * @author: lyj
     * @create: 2019/11/29
     **/
    public void sendReading(){
        MainActivity.send(BluetoothProtocol.SETTING_GET_DATA_PART1,new byte[]{});
        MainActivity.send(BluetoothProtocol.SETTING_GET_DATA_PART2,new byte[]{(byte) thresholdType});
    }

    /**
     * @description: 发送数据
     * @author: lyj
     * @create: 2019/11/27
     **/
    private void sendSetting(){
        if(thresholdHLFlag){
            byte[] send_threshold_hl=new byte[5];
            send_threshold_hl[0]=(byte)thresholdType;
            send_threshold_hl[1]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdHigh)[0];
            send_threshold_hl[2]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdHigh)[1];
            send_threshold_hl[3]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdLow)[0];
            send_threshold_hl[4]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdLow)[1];
            MainActivity.send(BluetoothProtocol.SETTING_THRESHOLD_LH,send_threshold_hl);
        }
        else if(amendFlag){
            byte[] send_amend=new byte[9];
            send_amend[0]=(byte)thresholdType;
            send_amend[1]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIAVal)[0];
            send_amend[2]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIAVal)[1];
            send_amend[3]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIBVal)[0];
            send_amend[4]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIBVal)[1];
            send_amend[5]=(byte)BluetoothProtocol.getIntArrayByTwo(NaICVal)[0];
            send_amend[6]=(byte)BluetoothProtocol.getIntArrayByTwo(NaICVal)[1];
            send_amend[7]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIDVal)[0];
            send_amend[8]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIDVal)[1];
            MainActivity.send(BluetoothProtocol.SETTING_AMEND,send_amend);
        }
        else if(collectTimeFlag){
            MainActivity.send(BluetoothProtocol.SETTING_COLLECT_TIME,new byte[]{(byte) timeVal});
        }
        else if(collectBGFlag){
            MainActivity.send(BluetoothProtocol.SETTING_COLLECT_BG_TIME,new byte[]{(byte)forceTimeVal});
        }
        else if(thresholdAlertFlag){
            byte[] send_threshold_alert=new byte[11];
            send_threshold_alert[0]=(byte)thresholdType;
            send_threshold_alert[1]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_1)[0];
            send_threshold_alert[2]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_1)[1];
            send_threshold_alert[3]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_2)[0];
            send_threshold_alert[4]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_2)[1];
            send_threshold_alert[5]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_3)[0];
            send_threshold_alert[6]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_3)[1];
            send_threshold_alert[7]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_4)[0];
            send_threshold_alert[8]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_4)[1];
            send_threshold_alert[9]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_5)[0];
            send_threshold_alert[10]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_5)[1];
            MainActivity.send(BluetoothProtocol.SETTING_THRESHOLD_ALERT,send_threshold_alert);
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
                Toast.makeText(getActivity().getApplicationContext(), "握手成功", Toast.LENGTH_LONG).show();
                if(isReading){
                    sendReading();
                }
                else{
                    sendSetting();
                }
                break;
            case BluetoothProtocol.SHAKE_HANDS_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "握手失败,无法设定", Toast.LENGTH_LONG).show();
                break;
            case BluetoothProtocol.SETTING_THRESHOLD_LH_OK:
                Toast.makeText(getActivity().getApplicationContext(), "设置高低本底阈值成功", Toast.LENGTH_LONG).show();
                thresholdHLFlag=false;
                sendSetting();
                MainActivity.settingThresholdHLState=BluetoothProtocol.NO_STATE;
                break;
            case BluetoothProtocol.SETTING_THRESHOLD_LH_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "设置高低本底阈值失败", Toast.LENGTH_LONG).show();
                thresholdHLFlag=false;
                sendSetting();
                break;
            case BluetoothProtocol.SETTING_AMEND_OK:
                Toast.makeText(getActivity().getApplicationContext(), "设置修正系数成功", Toast.LENGTH_LONG).show();
                amendFlag=false;
                sendSetting();
                MainActivity.settingAmendState=BluetoothProtocol.NO_STATE;
                break;
            case BluetoothProtocol.SETTING_AMEND_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "设置修正系数失败", Toast.LENGTH_LONG).show();
                amendFlag=false;
                sendSetting();
                break;
            case BluetoothProtocol.SETTING_COLLECT_TIME_OK:
                Toast.makeText(getActivity().getApplicationContext(), "设置能谱采集时间成功", Toast.LENGTH_LONG).show();
                collectTimeFlag=false;
                sendSetting();
                MainActivity.settingCollectTimeState=BluetoothProtocol.NO_STATE;
                break;
            case BluetoothProtocol.SETTING_COLLECT_TIME_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "设置能谱采集时间失败，请重新设置", Toast.LENGTH_LONG).show();
                collectTimeFlag=false;
                sendSetting();
                break;
            case BluetoothProtocol.SETTING_COLLECT_BG_TIME_CONTINUE:
                Toast.makeText(getActivity().getApplicationContext(), "等待第二段强制本底采集值", Toast.LENGTH_LONG).show();
                collectBGFlag=false;
                sendSetting();
                break;
            case BluetoothProtocol.SETTING_COLLECT_BG_TIME_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "强制本底采集值失败", Toast.LENGTH_LONG).show();
                collectBGFlag=false;
                sendSetting();
                break;
            case BluetoothProtocol.SETTING_THRESHOLD_ALERT_OK:
                Toast.makeText(getActivity().getApplicationContext(), "设置报警阈值成功", Toast.LENGTH_LONG).show();
                thresholdAlertFlag=false;
                sendSetting();
                MainActivity.settingThresholdAlertState=BluetoothProtocol.NO_STATE;
                break;
            case BluetoothProtocol.SETTING_THRESHOLD_ALERT_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "设置报警阈值失败", Toast.LENGTH_LONG).show();
                thresholdAlertFlag=false;
                sendSetting();
                break;
            case BluetoothProtocol.SETTING_GET_DATA_PART1_OK:
                readSettingPart1(data);
                MainActivity.settingGetDataPart1State=BluetoothProtocol.NO_STATE;
                break;
            case BluetoothProtocol.SETTING_GET_DATA_PART2_OK:
                readSettingPart2(data);
                MainActivity.settingGetDataPart2State=BluetoothProtocol.NO_STATE;
                break;
        }
    }
    /**
     * @description: 参数设置一
     * @author: lyj
     * @create: 2019/11/28
     **/
    public void readSettingPart1(int[] data){
        timeVal=data[1];
        time.setText(String.valueOf(timeVal));
        thresholdHigh=BluetoothProtocol.getVal(data,(thresholdType-1)*4+2,(thresholdType-1)*4+3);
        thresholdLow=BluetoothProtocol.getVal(data,(thresholdType-1)*4+4,(thresholdType-1)*4+5);
        threshold_high_val.setText(String.valueOf(thresholdHigh));
        threshold_low_val.setText(String.valueOf(thresholdLow));
        forceTimeVal=data[14];
        forceTime.setText(String.valueOf(forceTimeVal));
        NaIAVal=BluetoothProtocol.getVal(data,(thresholdType-1)*4+15,(thresholdType-1)*4+16);
        NaIBVal=BluetoothProtocol.getVal(data,(thresholdType-1)*4+17,(thresholdType-1)*4+18);
        NaI_a.setText(String.valueOf(NaIAVal));
        NaI_b.setText(String.valueOf(NaIBVal));
        Log.i(TAG, "处理读取数据一！！！！" );
    }

    /**
     * @description: 参数设置二
     * @author: lyj
     * @create: 2019/11/28
     **/
    public void readSettingPart2(int[] data){
        thresholdAlert_1=BluetoothProtocol.getVal(data,1,2);
        thresholdAlert_2=BluetoothProtocol.getVal(data,3,4);
        thresholdAlert_3=BluetoothProtocol.getVal(data,5,6);
        thresholdAlert_4=BluetoothProtocol.getVal(data,7,8);
        thresholdAlert_5=BluetoothProtocol.getVal(data,9,10);
        alert_threshold_val_1.setText(String.valueOf(thresholdAlert_1));
        alert_threshold_val_2.setText(String.valueOf(thresholdAlert_2));
        alert_threshold_val_3.setText(String.valueOf(thresholdAlert_3));
        alert_threshold_val_4.setText(String.valueOf(thresholdAlert_4));
        alert_threshold_val_5.setText(String.valueOf(thresholdAlert_5));
        Log.i(TAG, "处理读取数据二！！！！" );
    }
}


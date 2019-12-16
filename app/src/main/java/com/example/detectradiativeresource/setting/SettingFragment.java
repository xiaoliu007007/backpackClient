package com.example.detectradiativeresource.setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.utils.BluetoothProtocol;
import com.example.detectradiativeresource.utils.DataHelperUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SettingFragment extends Fragment{
    // 缓存Fragment view
    private static final String TAG = "SettingFragment";
    private View rootView;
    private static SettingFragment settingFragment;

    //private int alertVal;//报警时间
    private EditText alert;
    private int timeVal; //能谱采集时间
    private EditText time;
    private int forceTimeVal;//强制本底采集时间
    private EditText forceTime;
    private int alertFlag;//声光报警 0开1关
    //private int collectType;//测量单位，0计数率，1计量率
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

    private Button setting_sys;
    private Button reading_sys;
    private Button setting_dete;
    private Button reading_dete;
    private RadioButton alertFlagOpen;
    private RadioButton alertFlagClose;
    private RadioGroup alert_flag_rg;

    public static Timer timer = null;
    public static TimerTask task = null;
    private static boolean isSendFlag=false;
    private static boolean isFirstOpen=true;

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
        isSendFlag=false;
        isFirstOpen=true;
        sendReadingSys();
        //startTimer();
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        stopTimer();
    }

    private void init(View view){
        /*if(MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS_FAILED||MainActivity.receivedState==BluetoothProtocol.NO_STATE||
                MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS){
            MainActivity.send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
        }*/
        //BluetoothProtocol.isSendShakeHands();
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
        alert_flag_rg=(RadioGroup)view.findViewById(R.id.alert_flag_rg);
        final RadioGroup collect_type=(RadioGroup)view.findViewById(R.id.collect_type);
        final RadioGroup thresholdTyperg=(RadioGroup)view.findViewById(R.id.threshold_type);
        alertFlagOpen=view.findViewById(R.id.alert_flag_open);
        alertFlagClose=view.findViewById(R.id.alert_flag_close);
        alert_flag_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.alert_flag_open:
                        alertFlag=0;
                        MainActivity.isAlertMusic=true;
                        break;
                    case R.id.alert_flag_close:
                        alertFlag=1;
                        MainActivity.isAlertMusic=false;
                        break;
                }
            }
        });
        collect_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.collect_shu:
                        MainActivity.valType=1;
                        break;
                    case R.id.collect_liang:
                        MainActivity.valType=2;
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
                        sendReadingDete();
                        break;
                    case R.id.threshold_gm:
                        thresholdType=2;
                        sendReadingDete();
                        break;
                    case R.id.threshold_zz:
                        thresholdType=3;
                        sendReadingDete();
                        break;
                }
            }
        });
        reading_sys=(Button)view.findViewById(R.id.button_setting_read_1);
        reading_sys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReadingSys();
            }
        });
        reading_dete=(Button)view.findViewById(R.id.button_setting_read_2);
        reading_dete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReadingDete();
            }
        });
        setting_sys = (Button)view.findViewById(R.id.button_setting_1);
        setting_sys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ans="";
                if(!alert.getText().toString().equals("")){
                    MainActivity.closeAlertTime=Integer.parseInt(alert.getText().toString());
                    Log.i(TAG, "closeAlertTime is！！！！"+MainActivity.closeAlertTime);
                }
                if(time.getText().toString().equals("")){
                    timeVal=0;
                }
                else{
                    timeVal=Integer.parseInt(time.getText().toString());
                    MainActivity.spectrumTimeInterval=timeVal;
                }
                if(forceTime.getText().toString().equals("")){
                    forceTimeVal=0;
                }
                else{
                    forceTimeVal=Integer.parseInt(forceTime.getText().toString());
                }
                showbluetoothDialog1();
                //sendSettingSys();
            }
        });
        setting_dete = (Button)view.findViewById(R.id.button_setting_2);
        setting_dete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(threshold_high_val.getText().toString().equals("")){
                    thresholdHigh=0;
                }
                else{
                    thresholdHigh=Integer.parseInt(threshold_high_val.getText().toString());
                    thresholdLow=Integer.parseInt(threshold_low_val.getText().toString());
                }
                if(threshold_low_val.getText().toString().equals("")){
                    thresholdLow=0;
                }
                else{
                    thresholdLow=Integer.parseInt(threshold_low_val.getText().toString());
                }


                if(NaI_a.getText().toString().equals("")){
                    NaIAVal=0;
                }
                else{
                    NaIAVal=Integer.parseInt(NaI_a.getText().toString());
                }
                if(NaI_b.getText().toString().equals("")){
                    NaIBVal=0;
                }
                else{
                    NaIBVal=Integer.parseInt(NaI_b.getText().toString());
                }
                if(NaI_c.getText().toString().equals("")){
                    NaICVal=0;
                }
                else{
                    NaICVal=Integer.parseInt(NaI_c.getText().toString());
                }
                if(NaI_d.getText().toString().equals("")){
                    NaIDVal=0;
                }
                else{
                    NaIDVal=Integer.parseInt(NaI_d.getText().toString());
                }


                if(alert_threshold_val_1.getText().toString().equals("")){
                    thresholdAlert_1=0;
                }
                else{
                    thresholdAlert_1=Integer.parseInt(alert_threshold_val_1.getText().toString());
                    if(thresholdType==1){
                        MainActivity.alert_r_jiliang=thresholdAlert_1;
                    }
                    if(thresholdType==3){
                        MainActivity.alert_n_jishu=thresholdAlert_1;
                    }
                }
                if(alert_threshold_val_2.getText().toString().equals("")){
                    thresholdAlert_2=0;
                }
                else{
                    thresholdAlert_2=Integer.parseInt(alert_threshold_val_2.getText().toString());
                }
                if(alert_threshold_val_3.getText().toString().equals("")){
                    thresholdAlert_3=0;
                }
                else{
                    thresholdAlert_3=Integer.parseInt(alert_threshold_val_3.getText().toString());
                }
                showbluetoothDialog2();
                //sendSettingDete();
            }
        });
    }
    /**
     * @description: 读取数据1
     * @author: lyj
     * @create: 2019/11/29
     **/
    public void sendReadingSys(){
        if(isSendFlag){
            return;
        }
        isSendFlag=true;
        startTimer(1);
        //BluetoothProtocol.isSendShakeHands();
        //Log.i(TAG, "reading state is！！！！"+MainActivity.receivedState);
        /*MainActivity.send(BluetoothProtocol.SETTING_GET_DATA_PART1,new byte[]{});*/
    }

    /**
     * @description: 读取数据2
     * @author: lyj
     * @create: 2019/12/10
     **/
    public void sendReadingDete(){
        if(isSendFlag){
            //Log.i(TAG, "--------退出"+thresholdType);
            return;
        }
        //Log.i(TAG, "--------进入"+thresholdType);
        isSendFlag=true;
        startTimer(2);
        //BluetoothProtocol.isSendShakeHands();
        /*byte[] data1=new byte[1];
        data1[0]=(byte)thresholdType;
        MainActivity.send(BluetoothProtocol.SETTING_GET_DATA_PART2,data1);*/
    }

    /**
     * @description: 发送系统数据
     * @author: lyj
     * @create: 2019/12/10
     **/
    private void sendSettingSys(){
        if(isSendFlag){
            return;
        }
        isSendFlag=true;
        startTimer(3);
        //BluetoothProtocol.isSendShakeHands();
        /*byte[] data=new byte[3];
        data[0]=(byte)timeVal;
        data[1]=(byte)forceTimeVal;
        data[2]=(byte)alertFlag;
        MainActivity.send(BluetoothProtocol.SETTING_SET_DATA_PART1,data);*/
    }

    /**
     * @description: 发送探测器数据
     * @author: lyj
     * @create: 2019/12/10
     **/
    private void sendSettingDete(){
        if(isSendFlag){
            return;
        }
        if(thresholdAlert_2<=thresholdAlert_1){
            Toast.makeText(getActivity().getApplicationContext(), "阈值设置不正确", Toast.LENGTH_LONG).show();
            return;
        }
        if(thresholdAlert_3<=thresholdAlert_2){
            Toast.makeText(getActivity().getApplicationContext(), "阈值设置不正确", Toast.LENGTH_LONG).show();
            return;
        }
        isSendFlag=true;
        startTimer(4);
        //BluetoothProtocol.isSendShakeHands();
        /*byte[] data=new byte[23];
        data[0]=(byte)thresholdType;
        data[1]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdHigh)[0];
        data[2]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdHigh)[1];
        data[3]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdLow)[0];
        data[4]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdLow)[1];
        data[5]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIAVal)[0];
        data[6]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIAVal)[1];
        data[7]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIBVal)[0];
        data[8]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIBVal)[1];
        data[9]=(byte)BluetoothProtocol.getIntArrayByTwo(NaICVal)[0];
        data[10]=(byte)BluetoothProtocol.getIntArrayByTwo(NaICVal)[1];
        data[11]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIDVal)[0];
        data[12]=(byte)BluetoothProtocol.getIntArrayByTwo(NaIDVal)[1];
        data[13]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_1)[0];
        data[14]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_1)[1];
        data[15]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_2)[0];
        data[16]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_2)[1];
        data[17]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_3)[0];
        data[18]=(byte)BluetoothProtocol.getIntArrayByTwo(thresholdAlert_3)[1];
        MainActivity.send(BluetoothProtocol.SETTING_SET_DATA_PART2,data);*/
    }


    /**
     * @description: 处理接受数据
     * @author: lyj
     * @create: 2019/11/27
     **/
    public void handlerReceivedData(String Type,int[] data){
        isSendFlag=false;
        switch (Type) {
            /*case BluetoothProtocol.SHAKE_HANDS_OK:
                Toast.makeText(getActivity().getApplicationContext(), "握手成功", Toast.LENGTH_LONG).show();
                MainActivity.receivedState=BluetoothProtocol.SHAKE_HANDS_OK;
                break;
            case BluetoothProtocol.SHAKE_HANDS_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "握手失败,无法设定与读取", Toast.LENGTH_LONG).show();
                MainActivity.receivedState=BluetoothProtocol.SHAKE_HANDS_FAILED;
                break;*/
            case BluetoothProtocol.SETTING_GET_DATA_PART1_OK:
                readSettingPart1(data);
                isSendFlag=false;
                stopTimer();
                if(isFirstOpen){
                    sendReadingDete();
                }
                isFirstOpen=false;
                break;
            case BluetoothProtocol.SETTING_GET_DATA_PART1_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "获取系统设置失败，请重新读取", Toast.LENGTH_LONG).show();
                break;
            case BluetoothProtocol.SETTING_GET_DATA_PART2_OK:
                readSettingPart2(data);
                isSendFlag=false;
                stopTimer();
                break;
            case BluetoothProtocol.SETTING_GET_DATA_PART2_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "获取探测器设置失败，请重新读取", Toast.LENGTH_LONG).show();
                break;
            case BluetoothProtocol.SETTING_SET_DATA_PART1_OK:
                Toast.makeText(getActivity().getApplicationContext(), "系统设置成功", Toast.LENGTH_LONG).show();
                MainActivity.settingSetDataPart1State=BluetoothProtocol.SETTING_COLLECT_BG_TIME_CONTINUE;
                isSendFlag=false;
                stopTimer();
                break;
            case BluetoothProtocol.SETTING_SET_DATA_PART1_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "系统设置失败", Toast.LENGTH_LONG).show();
                break;
            case BluetoothProtocol.SETTING_SET_DATA_PART2_OK:
                Toast.makeText(getActivity().getApplicationContext(), "探测器设置成功", Toast.LENGTH_LONG).show();
                isSendFlag=false;
                stopTimer();
                break;
            case BluetoothProtocol.SETTING_SET_DATA_PART2_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "探测器设置失败", Toast.LENGTH_LONG).show();
                break;
        }
    }
    /**
     * @description: 参数设置一
     * @author: lyj
     * @create: 2019/11/28
     **/
    public void readSettingPart1(int[] data){
        //Log.i(TAG, "处理读取数据一！！！！" );
        timeVal=data[1];
        time.setText(String.valueOf(timeVal));
        forceTimeVal=data[2];
        forceTime.setText(String.valueOf(forceTimeVal));
        alertFlag=data[3];
        if(alertFlag==0){
            alertFlagOpen.setChecked(true);
        }
        else{
            alertFlagClose.setChecked(true);
        }
        //alert_flag_rg.check(alertFlag);
        //Toast.makeText(getActivity().getApplicationContext(), "声音报警"+alertFlag, Toast.LENGTH_LONG).show();
    }

    /**
     * @description: 参数设置二
     * @author: lyj
     * @create: 2019/11/28
     **/
    public void readSettingPart2(int[] data){
        thresholdHigh=BluetoothProtocol.getVal(data,4,5);
        threshold_high_val.setText(String.valueOf(thresholdHigh));
        thresholdLow=BluetoothProtocol.getVal(data,6,7);
        threshold_low_val.setText(String.valueOf(thresholdLow));

        NaIAVal=BluetoothProtocol.getVal(data,8,9);
        NaI_a.setText(String.valueOf(NaIAVal));
        NaIBVal=BluetoothProtocol.getVal(data,10,11);
        NaI_b.setText(String.valueOf(NaIBVal));
        NaICVal=BluetoothProtocol.getVal(data,12,13);
        NaI_c.setText(String.valueOf(NaICVal));
        NaIDVal=BluetoothProtocol.getVal(data,14,15);
        NaI_d.setText(String.valueOf(NaIDVal));


        thresholdAlert_1=BluetoothProtocol.getVal(data,16,17);
        thresholdAlert_2=BluetoothProtocol.getVal(data,18,19);
        thresholdAlert_3=BluetoothProtocol.getVal(data,20,21);
        alert_threshold_val_1.setText(String.valueOf(thresholdAlert_1));
        alert_threshold_val_2.setText(String.valueOf(thresholdAlert_2));
        alert_threshold_val_3.setText(String.valueOf(thresholdAlert_3));
        //Log.i(TAG, "处理读取数据二！！！！" );
    }

    //用户点击设置1
    private void showbluetoothDialog1() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getActivity());
        normalDialog.setTitle("系统设置");
        normalDialog.setMessage("是否进行系统设置？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendSettingSys();
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                });
        // 显示
        normalDialog.show();
    }

    //用户点击设置2
    private void showbluetoothDialog2() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getActivity());
        normalDialog.setTitle("探测器设置");
        normalDialog.setMessage("是否进行探测器设置？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendSettingDete();
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                });
        // 显示
        normalDialog.show();
    }

    public void startTimer(int msg) {
        if(timer == null) {
            timer = new Timer();
        }
        task=null;
        switch (msg){
            case 1:
                task = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                };
                break;
            case 2:
                task = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                };
                break;
            case 3:
                task = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                };
                break;
            case 4:
                task = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        Message message = new Message();
                        message.what = 4;
                        handler.sendMessage(message);
                    }
                };
                break;

        }
        timer.schedule(task, 100,1000);
    }

    public void stopTimer(){
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        if(task != null) {
            task.cancel();
            task = null;
        }
        Log.i(TAG, "-------------------stopTimer------------");
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    MainActivity.send(BluetoothProtocol.SETTING_GET_DATA_PART1,new byte[]{});
                    break;
                case 2:
                    byte[] ReadingPart2data=new byte[1];
                    ReadingPart2data[0]=(byte)thresholdType;
                    MainActivity.send(BluetoothProtocol.SETTING_GET_DATA_PART2,ReadingPart2data);
                    break;
                case 3:
                    byte[] settingPart1data=new byte[3];
                    settingPart1data[0]=(byte)timeVal;
                    settingPart1data[1]=(byte)forceTimeVal;
                    settingPart1data[2]=(byte)alertFlag;
                    MainActivity.send(BluetoothProtocol.SETTING_SET_DATA_PART1,settingPart1data);
                    break;
                case 4:
                    byte[] data=new byte[19];
                    data[0]=(byte)thresholdType;
                    data[1]=(byte)BluetoothProtocol.getIntArray(thresholdHigh,2)[0];
                    data[2]=(byte)BluetoothProtocol.getIntArray(thresholdHigh,2)[1];
                    data[3]=(byte)BluetoothProtocol.getIntArray(thresholdLow,2)[0];
                    data[4]=(byte)BluetoothProtocol.getIntArray(thresholdLow,2)[1];
                    data[5]=(byte)BluetoothProtocol.getIntArray(NaIAVal,2)[0];
                    data[6]=(byte)BluetoothProtocol.getIntArray(NaIAVal,2)[1];
                    data[7]=(byte)BluetoothProtocol.getIntArray(NaIBVal,2)[0];
                    data[8]=(byte)BluetoothProtocol.getIntArray(NaIBVal,2)[1];
                    data[9]=(byte)BluetoothProtocol.getIntArray(NaICVal,2)[0];
                    data[10]=(byte)BluetoothProtocol.getIntArray(NaICVal,2)[1];
                    data[11]=(byte)BluetoothProtocol.getIntArray(NaIDVal,2)[0];
                    data[12]=(byte)BluetoothProtocol.getIntArray(NaIDVal,2)[1];
                    data[13]=(byte)BluetoothProtocol.getIntArray(thresholdAlert_1,2)[0];
                    data[14]=(byte)BluetoothProtocol.getIntArray(thresholdAlert_1,2)[1];
                    data[15]=(byte)BluetoothProtocol.getIntArray(thresholdAlert_2,2)[0];
                    data[16]=(byte)BluetoothProtocol.getIntArray(thresholdAlert_2,2)[1];
                    data[17]=(byte)BluetoothProtocol.getIntArray(thresholdAlert_3,2)[0];
                    data[18]=(byte)BluetoothProtocol.getIntArray(thresholdAlert_3,2)[1];
                    MainActivity.send(BluetoothProtocol.SETTING_SET_DATA_PART2,data);
                    break;

            }
            super.handleMessage(msg);
        }
    };
}


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
    private int timeVal;
    private int thresholdVal;
    private int NaIAVal;
    private int NaIBVal;
    private int NaICVal;
    private int NaIDVal;
    private int thresholdType=1; //1,2,3NaI,GM,中子
    private int thresholdHLType=1; //1,2 低高本低
    private int NaIType=1; //1,2,3NaI,GM,中子

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
        final EditText time =(EditText) view.findViewById (R.id.collect_time);
        final EditText threshold =(EditText) view.findViewById (R.id.threshold_val);
        final EditText NaI_a =(EditText) view.findViewById (R.id.NaI_val_a);
        final EditText NaI_b =(EditText) view.findViewById (R.id.NaI_val_b);
        final EditText NaI_c =(EditText) view.findViewById (R.id.NaI_val_c);
        final EditText NaI_d =(EditText) view.findViewById (R.id.NaI_val_d);
        final RadioGroup thresholdHLTyperg=(RadioGroup)view.findViewById(R.id.threshold_hl);
        thresholdHLTyperg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.threshold_low:
                        thresholdHLType=1;
                        break;
                    case R.id.threshold_high:
                        thresholdHLType=2;
                        break;
                }
            }
        });

        final RadioGroup thresholdTyperg=(RadioGroup)view.findViewById(R.id.threshold_type);
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

        final RadioGroup NaITyperg=(RadioGroup)view.findViewById(R.id.Nai_type);
        NaITyperg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.Nai_type_nai:
                        NaIType=1;
                        break;
                    case R.id.Nai_type_gm:
                        NaIType=2;
                        break;
                    case R.id.Nai_type_zz:
                        NaIType=3;
                        break;
                }
            }
        });

        Button setting = (Button)view.findViewById(R.id.button_setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ans="";
                if(time.getText().toString().equals("")){
                    ans+="没有设置时间";
                }
                else{
                    timeVal=Integer.parseInt(time.getText().toString());
                    ans+="时间设置为"+timeVal;
                }

                if(threshold.getText().toString().equals("")){
                    ans+="没有设置阈值";
                }
                else{
                    switch (thresholdHLType){
                        case 1:
                            ans+="设置低本底和";
                            break;
                        case 2:
                            ans+="设置高本底和";
                            break;
                    }
                    switch (thresholdType){
                        case 1:
                            ans+="设置NaI阈值为：";
                            break;
                        case 2:
                            ans+="设置GM阈值为：";
                            break;
                        case 3:
                            ans+="设置中子阈值为：";
                            break;
                    }
                    thresholdVal=Integer.parseInt(threshold.getText().toString());
                    ans+=thresholdVal;
                }

                if(NaI_a.getText().toString().equals("")||NaI_b.getText().toString().equals("")||NaI_c.getText().toString().equals("")||NaI_d.getText().toString().equals("")){
                    ans+="A,B,C,D四个参数未都设置";
                }
                else{
                    switch (NaIType){
                        case 1:
                            ans+="设置NaI参数为";
                            break;
                        case 2:
                            ans+="设置GM参数为";
                            break;
                        case 3:
                            ans+="设置中子参数为";
                            break;
                    }
                    NaIAVal=Integer.parseInt(NaI_a.getText().toString());
                    NaIBVal=Integer.parseInt(NaI_b.getText().toString());
                    NaICVal=Integer.parseInt(NaI_c.getText().toString());
                    NaIDVal=Integer.parseInt(NaI_d.getText().toString());
                    ans+="A："+NaIAVal;
                    ans+="B："+NaIBVal;
                    ans+="C："+NaICVal;
                    ans+="D："+NaIDVal;
                }
                Toast.makeText(getActivity().getApplicationContext(), ans, Toast.LENGTH_LONG).show();
            }
        });
    }
}


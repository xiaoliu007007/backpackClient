package com.example.detectradiativeresource.spectrum;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.utils.BluetoothProtocol;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SpectrumFragment extends Fragment implements MainActivity.SpectrumListener {
    // 缓存Fragment view
    public static final String TAG="SpectrumFragment";
    private View rootView;
    private static SpectrumFragment spectrumFragment;
    private LineChart lineChart;
    private LineDataSet set1; //LineDataSet每一个对象就是一条连接线
    private ArrayList<Entry> data;//传递的数据
    private Button btn_spectrum;
    private TextView view_spectrum;
    private TextView msg_spectrum;
    private TextView type_spectrum;
    private boolean isStartSend=false;//是否准备发送
    private Timer timer = null;
    private TimerTask task = null;
    private Timer sendTimer = null;
    private TimerTask sendTask = null;
    private int times=10;


    public SpectrumFragment(){}
    public static SpectrumFragment getNewInstance(){
        if (spectrumFragment ==null){
            spectrumFragment =new SpectrumFragment();
        }
        return spectrumFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_spectrum,container, false);
        btn_spectrum=rootView.findViewById(R.id.btn_spectrum);
        view_spectrum=rootView.findViewById(R.id.view_spectrum);
        msg_spectrum=rootView.findViewById(R.id.msg_spectrum);
        type_spectrum=rootView.findViewById(R.id.type_spectrum);
        btn_spectrum.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(isStartSend){
                    stopSendTimer();
                    stopTimer();
                    btn_spectrum.setText("读谱");
                    view_spectrum.setText("倒计时");
                    isStartSend=false;
                }
                else{
                    sendData();
                    /*if(MainActivity.receivedState== BluetoothProtocol.NO_STATE||MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS_FAILED
                            ||MainActivity.receivedState==BluetoothProtocol.SHAKE_HANDS){
                        MainActivity.send(BluetoothProtocol.SHAKE_HANDS,new byte[]{});
                    }
                    else{
                        sendData();
                    }*/
                }
            }
        });
        initView(rootView);
        return rootView;
    }

    public void initView(View view){
        lineChart=rootView.findViewById(R.id.lineChart);
        //创建描述信息
        Description description =new Description();
        //description.setText("能谱线");
        description.setText("");
        description.setTextColor(Color.RED);
        description.setTextSize(20);
        lineChart.setDescription(description);//设置图表描述信息
        lineChart.setNoDataText("没有数据");//没有数据时显示的文字
        lineChart.setNoDataTextColor(Color.BLUE);//没有数据时显示文字的颜色
        lineChart.setDrawGridBackground(false);//chart 绘图区后面的背景矩形将绘制
        lineChart.setDrawBorders(false);//禁止绘制图表边框的线

        data = new ArrayList<>();
        data.add(new Entry(4,10));
        data.add(new Entry(6,15));
        data.add(new Entry(9,20));
        data.add(new Entry(12,5));
        data.add(new Entry(15,30));

        //判断图表中原来是否有数据
        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            //获取数据1
            set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set1.setValues(data);
            //刷新数据
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            //设置数据1  参数1：数据源 参数2：图例名称
            set1 = new LineDataSet(data, "能谱数据");
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);//设置线宽
            set1.setDrawValues(false);
            set1.setDrawCircles(false);
            //set1.setCircleRadius(0f);//设置焦点圆心的大小
            set1.enableDashedHighlightLine(10f, 5f, 0f);//点击后的高亮线的显示样式
            set1.setHighlightLineWidth(2f);//设置点击交点后显示高亮线宽
            set1.setHighlightEnabled(true);//是否禁用点击高亮线
            set1.setHighLightColor(Color.RED);//设置点击交点后显示交高亮线的颜色
            //set1.setValueTextSize(9f);//设置显示值的文字大小
            set1.setDrawFilled(false);//设置禁用范围背景填充


            //保存LineDataSet集合
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the datasets
            //创建LineData对象 属于LineChart折线图的数据集合
            LineData data = new LineData(dataSets);
            // 添加到图表中
            lineChart.setData(data);
            //绘制图表
            lineChart.invalidate();
        }

        lineChart.setTouchEnabled(true); // 设置是否可以触摸
        lineChart.setDragEnabled(true);// 是否可以拖拽
        lineChart.setScaleEnabled(false);// 是否可以缩放 x和y轴, 默认是true
        lineChart.setScaleXEnabled(true); //是否可以缩放 仅x轴
        lineChart.setScaleYEnabled(true); //是否可以缩放 仅y轴
        lineChart.setPinchZoom(true);  //设置x轴和y轴能否同时缩放。默认是否
        lineChart.setDoubleTapToZoomEnabled(true);//设置是否可以通过双击屏幕放大图表。默认是true
        lineChart.setHighlightPerDragEnabled(true);//能否拖拽高亮线(数据点与坐标的提示线)，默认是true
        lineChart.setDragDecelerationEnabled(true);//拖拽滚动时，手放开是否会持续滚动，默认是true（false是拖到哪是哪，true拖拽之后还会有缓冲）
        lineChart.setDragDecelerationFrictionCoef(0.99f);//与上面那个属性配合，持续滚动时的速度快慢，[0,1) 0代表立即停止。

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);//设置轴启用或禁用 如果禁用以下的设置全部不生效
        xAxis.setDrawAxisLine(true);//是否绘制轴线
        xAxis.setDrawGridLines(true);//设置x轴上每个点对应的线
        xAxis.setDrawLabels(true);//绘制标签  指x轴上的对应数值
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴的显示位置
        //xAxis.setTextSize(20f);//设置字体
        //xAxis.setTextColor(Color.BLACK);//设置字体颜色
        //设置竖线的显示样式为虚线
        //lineLength控制虚线段的长度
        //spaceLength控制线之间的空间
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setAvoidFirstLastClipping(true);//图表将避免第一个和最后一个标签条目被减掉在图表或屏幕的边缘
        xAxis.setLabelRotationAngle(10f);//设置x轴标签的旋转角度

        //获取右边的轴线
        YAxis rightAxis=lineChart.getAxisRight();
        //设置图表右边的y轴禁用
        rightAxis.setEnabled(false);
        //获取左边的轴线
        YAxis leftAxis = lineChart.getAxisLeft();
        //设置网格线为虚线效果
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        //是否绘制0所在的网格线
        leftAxis.setDrawZeroLine(false);
    }

    public void notifyChanged(){
        set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
        set1.setValues(data);
        set1.setDrawValues(false);
        set1.setDrawCircles(false);
        //刷新数据
        /*lineChart.getData().notifyDataChanged();
        lineChart.notifyDataSetChanged();*/
        //lineChart.invalidate();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                // 需要做的事:发送消息
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 0);
        Toast.makeText(getActivity().getApplicationContext(), "已经刷新最新能谱图", Toast.LENGTH_LONG).show();
        Log.i(TAG, "--------------刷新------------");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void notifyDataChanged(int[] num) {
        stopSendTimer();
        data.clear();
        for(int i=1;i<=num.length;i++){
            data.add(new Entry(i,num[i-1]));
        }
        notifyChanged();
        MainActivity.send(BluetoothProtocol.GET_DATA,new byte[]{});
    }

    public void sendData(){
        /*times=MainActivity.spectrumTimeInterval+1;
        btn_spectrum.setText(String.valueOf(times));*/
        times=MainActivity.spectrumTimeInterval+1;
        isStartSend=true;
        btn_spectrum.setText("停止");
        //btn_spectrum.setEnabled(false);
        startSendTimer();
        startTimer();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    //btn_spectrum.setText(String.valueOf(times));
                    view_spectrum.setText(String.valueOf(times));
                    //MainActivity.send(BluetoothProtocol.GET_SPECTRUM,new byte[]{});
                    startTimer();
                    break;
                case 2:
                    lineChart.getData().notifyDataChanged();
                    lineChart.notifyDataSetChanged();
                    lineChart.invalidate();
                    isStartSend=false;
                    btn_spectrum.setText("读谱");
                    view_spectrum.setText("倒计时");
                    break;
                case 3:
                    MainActivity.send(BluetoothProtocol.GET_SPECTRUM,new byte[]{});
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void handlerReceivedData(String Type,int[] data){
        switch (Type) {
            case BluetoothProtocol.GET_DATA:
                //DataHelperUtils.saveDataTotalMsg();
                UpdateUI(data);
                break;
            /*case BluetoothProtocol.SHAKE_HANDS_OK:
                MainActivity.receivedState=BluetoothProtocol.SHAKE_HANDS_OK;
                Log.i(TAG, "-------------------握手成功，开始读谱------------");
                sendData();
                break;
            case BluetoothProtocol.SHAKE_HANDS_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), "握手失败,请重新点击获取能谱", Toast.LENGTH_LONG).show();
                MainActivity.receivedState=BluetoothProtocol.SHAKE_HANDS_FAILED;
                break;*/
        }
    }

    public void UpdateUI(int[] data){
        String text="";
        int[] type=BluetoothProtocol.getTypeArrayByTwo(BluetoothProtocol.getVal(data,18,19));
        if(type[0]==1){
            text+="U235,";
        }
        if(type[1]==1){
            text+="Pu,";
        }
        if(type[2]==1){
            text+="U238,";
        }
        if(type[3]==1){
            text+="Th232,";
        }
        if(type[4]==1){
            text+="K40,";
        }
        if(type[5]==1){
            text+="Tl201,";
        }
        if(type[6]==1){
            text+="I131,";
        }
        if(type[7]==1){
            text+="Ga67,";
        }
        if(type[8]==1){
            text+="Tl204,";
        }
        if(type[9]==1){
            text+="Ra236,";
        }
        if(type[10]==1){
            text+="Ir162,";
        }
        if(type[11]==1){
            text+="Ba133,";
        }
        if(type[12]==1){
            text+="Cs137,";
        }
        if(type[13]==1){
            text+="Co57,";
        }
        if(type[14]==1){
            text+="Co60,";
        }
        if(type[15]==1){
            text+="Am241,";
        }
        if(text.equals("")){
            return;
        }
        text=text.substring(0,text.length()-1);
        msg_spectrum.setText(text);
        String msg="";
        switch (data[20]){
            case 0:
                msg="无";
                break;
            case 1:
                msg="天然";
                break;
            case 2:
                msg="人工";
                break;
        }
    }

    private void startTimer(){
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                // 需要做的事:发送消息
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        if(times!=0){
            timer.schedule(task, 1000);
        }
        else{

            //btn_spectrum.setEnabled(true);
            //isStartSend=false;
            //btn_spectrum.setText("读谱 ");
            return;
        }
        times--;
    }

    private void startSendTimer(){
        sendTimer = new Timer();
        sendTask = new TimerTask() {
            @Override
            public void run() {
                // 需要做的事:发送消息
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            }
        };
        sendTimer.schedule(sendTask,MainActivity.spectrumTimeInterval*1000,2000);
    }

    private void stopSendTimer(){
        if(sendTimer != null) {
            sendTimer.cancel();
            sendTimer = null;
        }

        if(sendTask != null) {
            sendTask.cancel();
            sendTask = null;
        }
    }

    private void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        if(task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        MainActivity.getSpectrum=BluetoothProtocol.NO_STATE;
        stopTimer();
        stopSendTimer();
        btn_spectrum.setText("读谱");
        isStartSend=false;
        //btn_spectrum.setEnabled(true);
    }
}


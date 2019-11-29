package com.example.detectradiativeresource.utils;

public class BluetoothProtocol {
    public static final String NO_STATE="no state";//无协议状态
    public static final String SHAKE_HANDS="shake hands";//握手协议
    public static final String SHAKE_HANDS_OK="shake hands ok";//握手成功
    public static final String SHAKE_HANDS_FAILED="shake hands failed";//握手失败
    public static final String START_MEASURE="start measure";//启动测量协议
    public static final String START_MEASURE_OK="start measure ok";//启动测量成功
    public static final String START_MEASURE_FAILED="start measure failed";//启动测量失败
    public static final String GET_DATA="get data";//获取数据协议
    public static final String STOP_MEASURE="stop measure";//停止测量协议
    public static final String STOP_MEASURE_OK="stop measure ok";//停止测量成功
    public static final String STOP_MEASURE_FAILED="stop measure failed";//停止测量失败

    public static final String ALERT_START="alert";//获得报警命令
    public static final String ALERT_CLOSE="alert close";//关闭报警
    public static final String ALERT_CLOSE_OK="alert close ok";//关闭报警成功
    public static final String ALERT_CLOSE_FAILED="alert close failed";//关闭报警失败

    public static final String SETTING_THRESHOLD_LH="setting threshold low high";//高低本底阈值命令
    public static final String SETTING_THRESHOLD_LH_OK="setting threshold low high ok";//高低本底阈值成功
    public static final String SETTING_THRESHOLD_LH_FAILED="setting threshold low high failed";//高低本底阈值失败
    public static final String SETTING_AMEND="setting amend";//NaI修正命令
    public static final String SETTING_AMEND_OK="setting amend ok";//NaI修正成功
    public static final String SETTING_AMEND_FAILED="setting amend failed";//NaI修正失败
    public static final String SETTING_COLLECT_TIME="setting collect time";//能谱采集时间设定
    public static final String SETTING_COLLECT_TIME_OK="setting collect time ok";//能谱采集时间成功
    public static final String SETTING_COLLECT_TIME_FAILED="setting collect time failed";//能谱采集时间失败
    public static final String SETTING_COLLECT_BG_TIME="setting collect background time";//强制采集本底命令
    public static final String SETTING_COLLECT_BG_TIME_OK="setting collect background time ok";//强制采集本底成功
    public static final String SETTING_COLLECT_BG_TIME_FAILED="setting collect background time failed";//强制采集本底失败
    public static final String SETTING_COLLECT_BG_TIME_CONTINUE="setting collect background time continue";//强制采集本底等待第二段
    public static final String SETTING_THRESHOLD_ALERT="setting threshold alert";//报警阈值命令
    public static final String SETTING_THRESHOLD_ALERT_OK="setting threshold alert ok";//报警阈值成功
    public static final String SETTING_THRESHOLD_ALERT_FAILED="setting threshold alert failed";//报警阈值失败

    public static final String SETTING_GET_DATA_PART1="setting get data part1";//获取第一部分参数内容
    public static final String SETTING_GET_DATA_PART1_OK="setting get data part1 ok";//获取第一部分参数成功
    public static final String SETTING_GET_DATA_PART1_FAILED="setting get data part1 failed";//获取第一部分参数失败
    public static final String SETTING_GET_DATA_PART2="setting get data part2";//获取第二部分参数内容
    public static final String SETTING_GET_DATA_PART2_OK="setting get data part2 ok";//获取第二部分参数成功
    public static final String SETTING_GET_DATA_PART2__FAILED="setting get data part2 ok";//获取第二部分参数失败

    public static final String GET_SPECTRUM="get spectrum";//获取能谱命令
    public static final String GET_SPECTRUM_OK="get spectrum ok";//获取能谱成功
    public static final String GET_SPECTRUM_FAILED="get spectrum failed";//获取能谱失败



    /**
     * @description:根据协议高低字节获取数据
     * @author: lyj
     * @create: 2019/11/27
     **/
    public static int getVal(int[] data,int start,int end){
        int ans=0;
        for(int i=start;i<=end;i++){
            ans+=data[i]*Math.pow(256,i-start);
        }
        return ans;
    }

    /**
     * @description:根据协议数据修改为高低字节
     * @author: lyj
     * @create: 2019/11/28
     **/
    public static int[] getIntArrayByTwo(int data){
        int[] ans=new int[2];
        for(int i=0;i<ans.length;i++){
            ans[i]=data%256;
            data/=256;
        }
        return ans;
    }
}

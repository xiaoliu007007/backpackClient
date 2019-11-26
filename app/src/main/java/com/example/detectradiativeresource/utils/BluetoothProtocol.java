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


    /**
     * @description:根据协议高低字节获取数据
     * @author: lyj
     * @create: 2019/11/12
     **/
    public static int getVal(int[] data,int start,int end){
        int ans=0;
        for(int i=start;i<=end;i++){
            ans+=data[i]*Math.pow(256,i-start);
        }
        return ans;
    }
}

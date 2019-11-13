package com.example.detectradiativeresource.dao;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * @description: 辐射值数据二级信息
 * @author: lyj
 * @create: 2019/09/02
 **/
public class DataMsg extends SugarRecord {
    @Unique
    String time;
    String value;
    double longitude;
    double latitude;
    int status; //历史点状态，1表式为绘制轨迹点
    String isAlarm;
    long parent;

    public DataMsg() {

    }

    public DataMsg(String time, String value, double longitude, double latitude, int status, String isAlarm, long parent) {
        this.time = time;
        this.value = value;
        this.longitude = longitude;
        this.latitude = latitude;
        this.status = status;
        this.isAlarm = isAlarm;
        this.parent = parent;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getIsAlarm() {
        return isAlarm;
    }

    public void setIsAlarm(String isAlarm) {
        this.isAlarm = isAlarm;
    }

    public long getParent() {
        return parent;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return time + '#'+value + '#' + longitude +"#"+ latitude;
    }

    public String myToString() {
        return "DataMsg{" +
                "time='" + time + '\'' +
                ", value='" + value + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", status=" + status +
                ", isAlarm='" + isAlarm + '\'' +
                ", parent=" + parent +
                '}';
    }
}

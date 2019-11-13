package com.example.detectradiativeresource.dao;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * @description: 辐射值数据一级信息
 * @author: lyj
 * @create: 2019/09/02
 **/

public class DataTotalMsg extends SugarRecord {
    @Unique
    String startTime;
    String endTime;
    String isAlarm;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getIsAlarm() {
        return isAlarm;
    }

    public void setIsAlarm(String isAlarm) {
        this.isAlarm = isAlarm;
    }
}

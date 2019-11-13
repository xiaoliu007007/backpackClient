package com.example.detectradiativeresource.dao;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * @description: 日志数据信息
 * @author: lyj
 * @create: 2019/10/15
 **/
public class LogMsg extends SugarRecord {
    @Unique
    private String type;
    private String startTime;
    private String endTime;

    public LogMsg() {
    }

    public LogMsg(String type, String startTime, String endTime) {
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    @Override
    public String toString() {
        return "LogMsg{" +
                "type='" + type + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}

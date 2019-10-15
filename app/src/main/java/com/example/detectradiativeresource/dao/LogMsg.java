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
    private String key;
    private String value;

    public LogMsg() {

    }

    public LogMsg(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LogMsg{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

package com.example.detectradiativeresource.dao;

import com.orm.SugarRecord;

public class LogDetailMsg extends SugarRecord {
    private String content;
    private String time;
    private long parent;

    public LogDetailMsg() {
    }

    public LogDetailMsg(String content, String time, long parent) {
        this.content = content;
        this.time = time;
        this.parent = parent;
    }

    public long getParent() {
        return parent;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "LogDetailMsg{" +
                "content='" + content + '\'' +
                ", time='" + time + '\'' +
                ", parent=" + parent +
                '}';
    }
}

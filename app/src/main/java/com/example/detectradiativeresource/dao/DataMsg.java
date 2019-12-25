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
    String NaI_jishu;
    String NaI_jiliang;
    String GM_jishu;
    String GM_jiliang;
    String n_jishu;
    String n_jiliang;
    double longitude;
    double latitude;
    int status; //历史点状态，0表示为普通状态，1表示为绘制轨迹点，2表示为预测点
    String isAlarm;
    long parent;

    public DataMsg() {

    }

    public DataMsg(String time, String naI_jishu, String naI_jiliang, String GM_jishu, String GM_jiliang, String n_jishu, String n_jiliang, double longitude, double latitude, int status, String isAlarm, long parent) {
        this.time = time;
        NaI_jishu = naI_jishu;
        NaI_jiliang = naI_jiliang;
        this.GM_jishu = GM_jishu;
        this.GM_jiliang = GM_jiliang;
        this.n_jishu = n_jishu;
        this.n_jiliang = n_jiliang;
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

    public String getNaI_jishu() {
        return NaI_jishu;
    }

    public void setNaI_jishu(String naI_jishu) {
        NaI_jishu = naI_jishu;
    }

    public String getNaI_jiliang() {
        return NaI_jiliang;
    }

    public void setNaI_jiliang(String naI_jiliang) {
        NaI_jiliang = naI_jiliang;
    }

    public String getGM_jishu() {
        return GM_jishu;
    }

    public void setGM_jishu(String GM_jishu) {
        this.GM_jishu = GM_jishu;
    }

    public String getGM_jiliang() {
        return GM_jiliang;
    }

    public void setGM_jiliang(String GM_jiliang) {
        this.GM_jiliang = GM_jiliang;
    }

    public String getN_jishu() {
        return n_jishu;
    }

    public void setN_jishu(String n_jishu) {
        this.n_jishu = n_jishu;
    }

    public String getN_jiliang() {
        return n_jiliang;
    }

    public void setN_jiliang(String n_jiliang) {
        this.n_jiliang = n_jiliang;
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
        return time + '#'+NaI_jishu + '#'+NaI_jiliang +'#'+GM_jishu +'#'+GM_jiliang +'#'+n_jishu +'#'+n_jiliang +'#' + longitude +"#"+ latitude+'#'+isAlarm+"#"+ status;
    }


}

package com.example.detectradiativeresource.dao;

public class DataMsgWrapper {
    public int index;
    public DataMsg dataMsg;

    public DataMsgWrapper() {
    }

    public DataMsgWrapper(int index, DataMsg dataMsg) {
        this.index = index;
        this.dataMsg = dataMsg;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public DataMsg getDataMsg() {
        return dataMsg;
    }

    public void setDataMsg(DataMsg dataMsg) {
        this.dataMsg = dataMsg;
    }

    @Override
    public String toString() {
        return "DataMsgWrapper{" +
                "index=" + index +
                ", dataMsg=" + dataMsg +
                '}';
    }
}

package com.example.detectradiativeresource.log;

import com.example.detectradiativeresource.dao.LogMsg;

import java.util.List;

public class LogMsgHelper {
    public static void logSave(String key,String value){
        LogMsg logMsg=new LogMsg(key,value);
        if(key.equals("本次使用时间")){
            List<LogMsg> list=LogMsg.find(LogMsg.class,"key = ?",key);
            if(list.size()!=0){
                LogMsg lastLogMsg=new LogMsg("上次使用时间",list.get(0).getValue());
                lastLogMsg.save();
            }
        }
        logMsg.save();
    }
}

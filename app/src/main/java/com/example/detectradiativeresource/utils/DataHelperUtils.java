package com.example.detectradiativeresource.utils;

import android.util.Log;

import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.dao.DataMsg;
import com.example.detectradiativeresource.dao.DataMsgWrapper;
import com.example.detectradiativeresource.dao.DataTotalMsg;
import com.example.detectradiativeresource.dao.LogDetailMsg;
import com.example.detectradiativeresource.dao.LogMsg;
import com.orm.query.Select;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DataHelperUtils {
    public static boolean dataTotalMsg_IsAlarm_Now=false; //当前一级data是否设置过报警
    public static long dataTotalMsg_Id_Now; //目前data最大的一级id，用于级联。

    /**
     * @description: 判断是否是新的一天
     * @author: lyj
     * @create: 2019/12/24
     **/
    public static boolean isNewDate(){
        if(DataTotalMsg.find(DataTotalMsg.class,"").size()==0){
            return true;
        }
        //List<DataTotalMsg> msg=DataTotalMsg.find(DataTotalMsg.class,"","","","order by startTime desc","limit 1");
        List<DataTotalMsg> msg= Select.from(DataTotalMsg.class).orderBy("start_time").list();
        if(msg.size()!=0){
            return !isTodayDate(msg.get(msg.size()-1).getStartTime());
        }
        return true;
    }

    /**
     * @description: 判断参数是否是今天
     * @author: lyj
     * @create: 2019/12/24
     **/
    public static boolean isTodayDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String today = dateFormat.format(new java.util.Date());
        return date.contains(today.substring(0,11));
    }

    /**
     * @description: 存储一级Data
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void saveDataTotalMsg(){
        Log.e("help", "-----------------save 1--------------");
        DataTotalMsg msg=new DataTotalMsg();
        msg.setStartTime(getTime());
        msg.setIsAlarm("否");
        msg.save();
        dataTotalMsg_Id_Now=msg.getId();
    }

    /**
     * @description: 更新一级Data的结束时间
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void updateDataTotalMsgTime(){
        //dataTotalMsg_Id_Now = DataTotalMsg.find(DataTotalMsg.class,"").size();
        List<DataTotalMsg> listHelp=DataTotalMsg.find(DataTotalMsg.class,"");
        if(listHelp.size()==0){
            dataTotalMsg_Id_Now=0;
        }
        else{
            dataTotalMsg_Id_Now=listHelp.get(listHelp.size()-1).getId();
        }
        DataTotalMsg msg=DataTotalMsg.findById(DataTotalMsg.class,dataTotalMsg_Id_Now);
        if(msg!=null){
            msg.setEndTime(getTime());
            msg.save();
        }
    }

    /**
     * @description: 更新一级Data的是否报警标志
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void updateDataTotalMsgIsAlarm(){
        if(!dataTotalMsg_IsAlarm_Now){
            //dataTotalMsg_Id_Now = DataTotalMsg.find(DataTotalMsg.class,"").size();
            List<DataTotalMsg> listHelp=DataTotalMsg.find(DataTotalMsg.class,"");
            if(listHelp.size()==0){
                dataTotalMsg_Id_Now=0;
            }
            else{
                dataTotalMsg_Id_Now=listHelp.get(listHelp.size()-1).getId();
            }
            DataTotalMsg msg=DataTotalMsg.findById(DataTotalMsg.class,dataTotalMsg_Id_Now);
            msg.setIsAlarm("是");
            msg.save();
        }
        dataTotalMsg_IsAlarm_Now=true;
    }

    /**
     * @description: 获取选中list的一级Data的Id
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static long findDataTotalMsgId(int position){
        DataTotalMsg msg = DataTotalMsg.listAll(DataTotalMsg .class).get(position);
        return msg!=null?msg.getId():-1;
    }

    /**
     * @description: 存储二级Data
     * @author: lyj
     * @create: 2019/09/02
     **/
    public static void saveDataMsg(String Nai_jishu,String Nai_jiliang,String GM_jishu,String GM_jiliang,String n_jishu,String n_jiliang,double longitude,double latitude,int status,boolean flag){
        if(longitude!=0.0&&latitude!=0.0){
            String isAlarm=flag?"是":"否";
            List<DataTotalMsg> listHelp=DataTotalMsg.find(DataTotalMsg.class,"");
            if(listHelp.size()==0){
                dataTotalMsg_Id_Now=0;
            }
            else{
                dataTotalMsg_Id_Now=listHelp.get(listHelp.size()-1).getId();
            }
            DataMsg msg=new DataMsg(getTime(),Nai_jishu,Nai_jiliang,GM_jishu,GM_jiliang,n_jishu,n_jiliang,longitude,latitude,status,isAlarm,dataTotalMsg_Id_Now);
            Log.i("------save id is-----",String.valueOf(dataTotalMsg_Id_Now));
            msg.save();
        }
        Log.e("help", "-----------------save 2--------------");
    }

    /**
     * @description: 级联删除数据，从一级Data id删除所有级联数据
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void deleteDataTotalMsgById(long id){
        DataTotalMsg msg=DataTotalMsg.findById(DataTotalMsg.class,id);
        msg.delete();
        List<DataMsg > list = DataMsg.find(DataMsg.class,"parent = ?",String.valueOf(id));
        for(DataMsg data:list){
            data.delete();
        }

        Log.e("help", "------------------------------delete id is"+id);
        List<DataMsg > list1 = DataMsg.find(DataMsg.class,"");
        for(DataMsg data:list1){
            Log.e("help", "------------------------------parent id is"+data.getParent());
        }
    }

    /**
     * @description: 查询所有二级Data
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static List<DataMsg > findAllDataMsg(){
        List<DataMsg > list = DataMsg.find(DataMsg.class,"");
        return list;
    }

    /**
     * @description: 根据一级id获取所有二级data数据
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static List<DataMsg > findDataMsgByParent(long id){
        List<DataMsg > list = DataMsg.find(DataMsg.class,"parent = ?",String.valueOf(id));
        return list;
    }

    /**
     * @description: 根据二级List选中的部分获取二级data
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static long findDataMsgId(long id,int position){
        DataMsg msg = DataMsg.find(DataMsg.class,"parent = ?",String.valueOf(id)).get(position);
        return msg!=null?msg.getId():-1;
    }

    /**
     * @description: 根据二级id删除数据
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void deleteDataDetailMsgId(long id){
        DataMsg msg=DataMsg.findById(DataMsg.class,id);
        msg.delete();
    }

    /**
     * @description: 获取当天status为2的所有数据的list
     * @author: lyj
     * @create: 2019/12/25
     **/
    public static ArrayList<ArrayList<Double>> findDataByNowDateAsList(){
        List<DataMsg> msg = DataMsg.find(DataMsg.class,"status = ?",String.valueOf(2));
        ArrayList<ArrayList<Double>> ans=new ArrayList<>();
        for(DataMsg data:msg){
            if(isTodayDate(data.getTime())){
                ArrayList<Double> list=new ArrayList<>();
                list.add(data.getLongitude());
                list.add(data.getLatitude());
                list.add(Double.parseDouble(data.getNaI_jishu()));
                ans.add(list);
            }
        }
        return ans;
    }

    /**
     * @description: 获取当天status为2的所有数据
     * @author: lyj
     * @create: 2019/12/25
     **/
    public static ArrayList<DataMsg> findDataByNowDate(){
        List<DataMsg> msg = DataMsg.find(DataMsg.class,"");
        ArrayList<DataMsg> ans=new ArrayList<>();
        for(DataMsg data:msg){
            if(isTodayDate(data.getTime())){
                ans.add(data);
            }
        }
        return ans;
    }





    /**************************************日志部分的CRUD*************************************/

    /**
     * @description: 存储日志部分，包括了如果有type，更新其结束时间，如果没有则创建，但一定会根据一级log的id创建二级log
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void saveLogMsg(String type,String content){
        List<LogMsg > list=findLogMsgByType(type);
        if(list.size()==0){
            LogMsg msg=new LogMsg();
            msg.setType(type);
            msg.setStartTime(getTime());
            msg.setEndTime(getTime());
            msg.save();
        }
        else{
            updateLogMsgEndTimeByType(type);
        }
        list=findLogMsgByType(type);
        if(list.size()!=0){
            saveLogDetailMsgByContent(content,list.get(0).getId());
        }
    }

    /**
     * @description: 根据type名称获取所有一级log
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static List<LogMsg > findLogMsgByType(String type){
        List<LogMsg > list = LogMsg.find(LogMsg.class,"type = ?",type);
        return list;
    }

    /**
     * @description: 根据type名称更新一级log的结束时间
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void updateLogMsgEndTimeByType(String type){
        List<LogMsg > list = LogMsg.find(LogMsg.class,"type = ?",type);
        if(list!=null){
            LogMsg msg=list.get(0);
            msg.setEndTime(getTime());
            msg.save();
        }
    }

    /**
     * @description: 根据一级Log的id和更新的具体内容，更新二级Log
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void saveLogDetailMsgByContent(String content,long parent){
        LogDetailMsg msg=new LogDetailMsg();
        msg.setContent(content);
        msg.setTime(getTime());
        msg.setParent(parent);
        msg.save();
    }

    /**
     * @description: 获取所有的一级log
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static List<LogMsg> findAllLogMsg(){
        return LogMsg.listAll(LogMsg.class);
    }

    /**
     * @description: 根据一级list的选中，获取选中的一级Log的Id
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static long findLogMsgId(int position){
        LogMsg msg = LogMsg.listAll(LogMsg .class).get(position);
        return msg!=null?msg.getId():-1;
    }

    /**
     * @description: 根据一级log的id获取所有的二级Log
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static List<LogDetailMsg > findLogDetailMsgByParent(long id){
        List<LogDetailMsg > list = LogDetailMsg.find(LogDetailMsg.class,"parent = ?",String.valueOf(id));
        return list;
    }

    /**
     * @description: 获取所有的二级log
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static List<LogDetailMsg> findAllLogDetailMsg(){
        return LogDetailMsg.listAll(LogDetailMsg.class);
    }

    /**
     * @description: 级联删除数据，根据一级log的id删除所有的一级log和对应的二级Log
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void deleteLogMsgById(long id){
        LogMsg msg=LogMsg.findById(LogMsg.class,id);
        msg.delete();
        List<LogDetailMsg > list = LogDetailMsg.find(LogDetailMsg.class,"parent = ?",String.valueOf(id));
        for(LogDetailMsg data:list){
            data.delete();
        }

    }

    /**
     * @description: 根据id和选中的二级列表的位置获取对应的二级Log的id
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static long findLogDetailMsgId(long id,int position){
        LogDetailMsg msg = LogDetailMsg.find(LogDetailMsg.class,"parent = ?",String.valueOf(id)).get(position);
        return msg!=null?msg.getId():-1;
    }

    /**
     * @description: 根据二级log的id删除对应的二级log
     * @author: lyj
     * @create: 2019/11/13
     **/
    public static void deleteLogDetailMsgId(long id){
        LogDetailMsg msg=LogDetailMsg.findById(LogDetailMsg.class,id);
        msg.delete();
    }

    /**
     * @description: 获取当前系统时间
     * @author: lyj
     * @create: 2019/09/02
     **/
    public static String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = dateFormat.format(new java.util.Date());
        return date;
    }
}

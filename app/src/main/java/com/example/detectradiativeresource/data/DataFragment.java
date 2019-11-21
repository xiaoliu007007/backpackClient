package com.example.detectradiativeresource.data;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.dao.DataMsg;
import com.example.detectradiativeresource.dao.DataTotalMsg;
import com.example.detectradiativeresource.route.BNaviMainActivity;
import com.example.detectradiativeresource.utils.DataHelperUtils;
import com.example.detectradiativeresource.utils.FragmentChangeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 数据管理二级列表
 * @author: lyj
 * @create: 2019/11/13
 **/
public class DataFragment extends Fragment{
    private static final String TAG = "DataFragment";
    // 缓存Fragment view
    private View rootView;
    private static DataFragment dataFragment;
    private ListView infoList;
    private DataMsgAdapter mAdapter;
    private List<DataMsg> mdata;
    public DataTotalFragmentChangeListener myListener;

    public DataFragment(){}
    public static DataFragment getNewInstance(){
        if (dataFragment ==null){
            dataFragment =new DataFragment();
        }
        return dataFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_data, container, false);
        init(rootView);
        return rootView;
    }
    private void init(View view){
        infoList = (ListView) view.findViewById(R.id.data_list);
        mdata = DataHelperUtils.findDataMsgByParent(FragmentChangeUtils.dataMsgId);
        mAdapter = new DataMsgAdapter(getActivity(), mdata);
        infoList.setAdapter(mAdapter);
        Button reButton = (Button)view.findViewById(R.id.data_return);
        reButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentChangeUtils.isDataTotalMsgFragment=true;
                myListener.changeDataFragment();
            }
        });
        infoList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "删除");

            }
        });
    }
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int position=info.position;
        switch (item.getItemId()) {
            case 0:
                long deleteId= DataHelperUtils.findDataMsgId(FragmentChangeUtils.dataMsgId,position);
                if(deleteId>0){
                    DataHelperUtils.deleteDataDetailMsgId(deleteId);
                }
                init(rootView);
                break;
        }
        return super.onContextItemSelected(item);
    }
    public interface DataTotalFragmentChangeListener{
        public void changeDataFragment();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myListener=(DataTotalFragmentChangeListener)activity;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /*private List<DataMsg> find(){
        List<DataMsg > msg = DataMsg.find(DataMsg.class,"parent = ?",String.valueOf(FragmentChangeUtils.dataMsgId));
        return msg;
    }*/
}


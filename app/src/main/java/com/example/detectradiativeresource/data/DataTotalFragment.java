package com.example.detectradiativeresource.data;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

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

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

public class DataTotalFragment extends Fragment{
    // 缓存Fragment view
    private static final String TAG = "DataTotalFragment";
    private View rootView;
    private static DataTotalFragment dataFragment;
    private ListView infoList;
    private DataTotalMsgAdapter mAdapter;
    private List<DataTotalMsg> mdata;
    public static final int SHOW_ANS=0;
    public DataFragmentChangeListener myListener;

    public DataTotalFragment(){}
    public static DataTotalFragment getNewInstance(){
        if (dataFragment ==null){
            dataFragment =new DataTotalFragment();
        }
        return dataFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_datatotal, container, false);
        init(rootView);
        return rootView;
    }
    private void init(View view){
        infoList = (ListView) view.findViewById(R.id.data_total_list);
        mdata = find();
        mAdapter = new DataTotalMsgAdapter(getActivity(), mdata);
        infoList.setAdapter(mAdapter);
        Button upload = (Button)view.findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMsg(DataHelperUtils.findAllDataMsg());
            }
        });
        infoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                long id= DataHelperUtils.findDataTotalMsgId(i);
                if(id>0){
                    FragmentChangeUtils.dataMsgId=id;
                    FragmentChangeUtils.isDataTotalMsgFragment=false;
                    myListener.changeDataFragment();
                }
            }
        });
        infoList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "查看");
                menu.add(0, 1, 0, "删除");
                menu.add(0, 2, 0, "上传");

            }
        });
    }
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int position=info.position;
        switch (item.getItemId()) {
            case 0:
                long id= DataHelperUtils.findDataTotalMsgId(position);
                if(id>0){
                    FragmentChangeUtils.dataMsgId=id;
                    FragmentChangeUtils.isDataTotalMsgFragment=false;
                    myListener.changeDataFragment();
                }
                break;
            case 1:
                long deleteId= DataHelperUtils.findDataTotalMsgId(position);
                if(deleteId>0){
                    DataHelperUtils.deleteDataTotalMsgById(deleteId);
                }
                init(rootView);
                break;
            case 2:
                long sendId= DataHelperUtils.findDataTotalMsgId(position);
                if(sendId>0){
                    sendMsg(DataHelperUtils.findDataMsgByParent(sendId));
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myListener=(DataFragmentChangeListener)activity;
    }

    public interface DataFragmentChangeListener{
        public void changeDataFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private List<DataTotalMsg> find(){
        List<DataTotalMsg > msg = DataTotalMsg.listAll(DataTotalMsg .class);
        return msg;
    }
    private void sendMsg(final List<DataMsg> list){
        DataHelperUtils.saveLogMsg("上传","上传数据");
        new Thread(new Runnable() {
            @Override
            public void run(){
                HttpClient httpClient = new DefaultHttpClient();
                String url=MainActivity.IP+"getMsg/";
                HttpPost httpPost = new HttpPost(url);
                ArrayList<NameValuePair> datas = new ArrayList<NameValuePair>();
                for(DataMsg msg:list){
                    NameValuePair data = new BasicNameValuePair("name", msg.toString());
                    datas.add(data);
                }
                try {
                    HttpEntity requestEntity = new UrlEncodedFormEntity(datas);
                    httpPost.setEntity(requestEntity);
                    try {
                        HttpResponse response = httpClient.execute(httpPost);
                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            String result = EntityUtils.toString(response.getEntity());
                            Message message = new Message();
                            message.what = SHOW_ANS;
                            message.obj = result;
                            if(result!=null && result.length()!=0){
                                handler.sendMessage(message);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_ANS:
                    JSONObject ans = null;
                    try {
                        ans = new JSONObject((String) msg.obj);
                        Intent intent = new Intent(getActivity(), BNaviMainActivity.class);
                        intent.putExtra("end_longitude",ans.get("longitude").toString());
                        intent.putExtra("end_latitude",ans.get("latitude").toString());
                        intent.putExtra("start_longitude", MainActivity.longitude);
                        intent.putExtra("start_latitude", MainActivity.latitude);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;

            }

        }
    };
}


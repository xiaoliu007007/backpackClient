package com.example.detectradiativeresource.data;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.detectradiativeresource.MainActivity;
import com.example.detectradiativeresource.R;
import com.example.detectradiativeresource.dao.DataMsg;
import com.example.detectradiativeresource.route.BNaviMainActivity;

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

public class DataFragment extends Fragment{
    // 缓存Fragment view
    private View rootView;
    private static DataFragment dataFragment;
    private ListView infoList;
    private DataMsgAdapter mAdapter;
    private List<DataMsg> mdata;
    public static final int SHOW_ANS=0;

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
        infoList = (ListView) view.findViewById(R.id.datalist);
        mdata = find();
        mAdapter = new DataMsgAdapter(getActivity(), mdata);
        infoList.setAdapter(mAdapter);
        Button upload = (Button)view.findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMsg();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private List<DataMsg> find(){
        List<DataMsg > msg = DataMsg.listAll(DataMsg .class);
        return msg;
    }
    private void sendMsg(){
        new Thread(new Runnable() {
            @Override
            public void run(){
                HttpClient httpClient = new DefaultHttpClient();
                String url=MainActivity.IP+"getMsg/";
                HttpPost httpPost = new HttpPost(url);
                List<DataMsg> list=find();
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


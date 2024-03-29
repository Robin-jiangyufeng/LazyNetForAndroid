package com.robin.lazy.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.robin.lazy.net.http.HttpRequestManager;
import com.robin.lazy.net.http.RequestLifecycleContext;
import com.robin.lazy.net.http.cache.CacheTextResponseListener;
import com.robin.lazy.net.http.cache.HttpCacheLoadType;
import com.robin.lazy.net.http.core.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class GetActivity extends AppCompatActivity implements RequestLifecycleContext,CacheTextResponseListener {

    private TextView textView;
    private EditText editText;
    private HttpRequestManager httpRequestManager;
    private boolean isUseCache;
    private long lastTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpRequestManager=new HttpRequestManager(this);
        if(getIntent()!=null){
            isUseCache=getIntent().getBooleanExtra("isUseCache",false);
        }
        setContentView(R.layout.activity_get);
        textView=(TextView)findViewById(R.id.textView);
        editText=(EditText)findViewById(R.id.editText);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<100;i++){
                    RequestParam param=new RequestParam(new Random().nextInt(),editText.getText().toString());
                    param.setDelayTime(200);
                    param.addSendData("key", "03222bd3467ec2aa045aef63cd134a9e");
                    if(isUseCache){
                        httpRequestManager.sendCacheHttpGetRequest(GetActivity.this,param,
                                new DefaultLoadingView(getCurrContext()),
                                GetActivity.this,
                                HttpCacheLoadType.USE_CACHE_UPDATE_CACHE);
                    }else{
                        httpRequestManager.sendHttpGetRequest(GetActivity.this,param,new DefaultLoadingView(getCurrContext()),GetActivity.this);
                    }
                    lastTime=System.currentTimeMillis();
                }
            }
        });
    }

    @Override
    public Context getCurrContext() {
        return this;
    }

    @Override
    public void onStart(int messageId) {

    }

    @Override
    public void onLoadCache(int messageId, Map<String, List<String>> headers, String cacheData) {

    }

    @Override
    public void onSuccess(int messageId, Map<String, List<String>> headers, String data) {
        Toast.makeText(this,"onSuccess"+(System.currentTimeMillis()-lastTime)+"毫秒",Toast.LENGTH_SHORT).show();
        textView.setText(data);
    }

    @Override
    public void onFail(int messageId, int statusCode, Map<String, List<String>> headers, String data) {
        Toast.makeText(this,"onFail"+(System.currentTimeMillis()-lastTime)+"毫秒",Toast.LENGTH_SHORT).show();
        textView.setText(data);
    }
}

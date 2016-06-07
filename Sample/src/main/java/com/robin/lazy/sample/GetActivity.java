package com.robin.lazy.sample;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.robin.lazy.net.http.HttpRequestManager;
import com.robin.lazy.net.http.RequestLifecycleContext;
import com.robin.lazy.net.http.TextResponseListener;
import com.robin.lazy.net.http.core.RequestParam;

import java.util.List;
import java.util.Map;

public class GetActivity extends AppCompatActivity implements RequestLifecycleContext,TextResponseListener {

    private TextView textView;
    private EditText editText;
    private HttpRequestManager httpRequestManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpRequestManager=HttpRequestManager.getInstance(this);
        setContentView(R.layout.activity_get);
        textView=(TextView)findViewById(R.id.textView);
        editText=(EditText)findViewById(R.id.editText);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParam param=new RequestParam(101,editText.getText().toString());
                param.addSendData("key","03222bd3467ec2aa045aef63cd134a9e");
                httpRequestManager.sendHttpGetRequest(GetActivity.this,param,new DefaultLoadingView(getContext()),GetActivity.this);
            }
        });
    }

    @Override
    public Activity getContext() {
        return this;
    }

    @Override
    public void onStart(int messageId) {

    }

    @Override
    public void onSuccess(int messageId, Map<String, List<String>> headers, String data) {
        textView.setText(data);
    }

    @Override
    public void onFail(int messageId, int statusCode, Map<String, List<String>> headers, String data) {
        textView.setText(data);
    }
}

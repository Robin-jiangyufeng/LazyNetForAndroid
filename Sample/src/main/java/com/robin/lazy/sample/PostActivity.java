package com.robin.lazy.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.robin.lazy.net.http.HttpRequestManager;
import com.robin.lazy.net.http.RequestLifecycleContext;
import com.robin.lazy.net.http.cache.CacheResponseListener;
import com.robin.lazy.net.http.cache.HttpCacheLoadType;
import com.robin.lazy.net.http.core.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PostActivity extends AppCompatActivity implements RequestLifecycleContext, CacheResponseListener<String,String> {

    private TextView textView;
    private EditText editText;
    private EditText etAccount;
    private EditText etPassword;
    private HttpRequestManager httpRequestManager;
    private boolean isUseCache;
    private long lastTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpRequestManager = new HttpRequestManager(this);
        if (getIntent() != null) {
            isUseCache = getIntent().getBooleanExtra("isUseCache", false);
        }
        setContentView(R.layout.activity_post);
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        etAccount = (EditText) findViewById(R.id.etAccount);
        etPassword = (EditText) findViewById(R.id.etPassword);
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParam param = new TestRequestParam(new Random().nextInt(), editText.getText().toString());
                param.addHeader("Content-Type", "application/json; charset=utf-8");

                param.setDelayTime(200);
                param.addSendData("deviceSn", "VB01192D01407");
                param.addSendData("deviceVersion", "1.2.3");
                param.addSendData("password", etPassword.getText().toString());
                param.addSendData("phoneNumber", etAccount.getText().toString());
                if (isUseCache) {
                    httpRequestManager.sendCacheHttpPostRequest(PostActivity.this, param,
                            new DefaultLoadingView(getCurrContext()),
                            PostActivity.this,
                            HttpCacheLoadType.USE_CACHE_UPDATE_CACHE);
                } else {
                    httpRequestManager.sendHttpPostRequest(PostActivity.this, param, new DefaultLoadingView(getCurrContext()), PostActivity.this);
                }
                lastTime = System.currentTimeMillis();
            }
        });
    }

    @Override
    public Activity getCurrContext() {
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
        Toast.makeText(this, "onSuccess" + (System.currentTimeMillis() - lastTime) + "毫秒", Toast.LENGTH_SHORT).show();
        textView.setText(data);
    }

    @Override
    public void onFail(int messageId, int statusCode, Map<String, List<String>> headers, String data) {
        Toast.makeText(this, "onFail" + (System.currentTimeMillis() - lastTime) + "毫秒", Toast.LENGTH_SHORT).show();
        textView.setText(data);
    }
}

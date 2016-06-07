package com.robin.lazy.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.robin.lazy.net.state.NetChangeObserver;
import com.robin.lazy.net.state.NetWorkUtil;
import com.robin.lazy.net.state.NetworkStateReceiver;

public class MainActivity extends AppCompatActivity implements NetChangeObserver, View.OnClickListener {

    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        snackbar = Snackbar.make(fab, "当前没有网络", Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkStateReceiver.checkNetworkState(MainActivity.this);
            }
        });
        NetworkStateReceiver.registerNetworkStateReceiver(this);
        NetworkStateReceiver.registerObserver(this);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.button:
                startActivity(new Intent(this,GetActivity.class));
                break;
            case R.id.button2:
                break;
            case R.id.button3:
                break;
            case R.id.button4:
                break;
            case R.id.button5:
                break;
            case R.id.button6:
                break;
            default:
        }
    }

    @Override
    public void onConnect(NetWorkUtil.NetType type) {
        snackbar.setText("网络状态改变:type==" + type.name());
        snackbar.show();
    }

    @Override
    public void onDisConnect() {
        snackbar.setText("当前没有网络");
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkStateReceiver.unregisterNetworkStateReceiver(this);
        NetworkStateReceiver.unregisterObserver(this);
        NetworkStateReceiver.close();
    }
}

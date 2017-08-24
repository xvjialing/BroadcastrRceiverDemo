package com.lytech.xvjialing.broadcastreceiverdemo;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private MyReceiver myReceiver;
    private Button btn_sendMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sendMsg = (Button) findViewById(R.id.btn_sendMsg);

        btn_sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });

        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        myReceiver=new MyReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("android.intent.action.MY_BROADCAST");

        registerReceiver(myReceiver,intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    private void send(){
        Intent intent=new Intent("android.intent.action.MY_BROADCAST");
        intent.putExtra("msg","hello receiver");
        sendBroadcast(intent);
    }
}

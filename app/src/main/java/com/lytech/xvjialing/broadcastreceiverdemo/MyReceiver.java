package com.lytech.xvjialing.broadcastreceiverdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG=MyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg=intent.getStringExtra("msg");
        Log.d(TAG, "onReceive: "+msg);
    }
}

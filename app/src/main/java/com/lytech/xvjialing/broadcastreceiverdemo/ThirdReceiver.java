package com.lytech.xvjialing.broadcastreceiverdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ThirdReceiver extends BroadcastReceiver {

    private static final String TAG = ThirdReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg=getResultExtras(true).getString("msg");
        Log.d(TAG, "onReceive: "+msg);

    }
}

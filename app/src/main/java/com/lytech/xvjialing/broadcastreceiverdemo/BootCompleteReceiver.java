package com.lytech.xvjialing.broadcastreceiverdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = BootCompleteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service=new Intent(context,MsgPushService.class);
        context.startService(service);
        Log.d(TAG, "Boot Complete. Starting MsgPushService...");
    }
}

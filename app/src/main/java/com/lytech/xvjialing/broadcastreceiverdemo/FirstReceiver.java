package com.lytech.xvjialing.broadcastreceiverdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FirstReceiver extends BroadcastReceiver {

    private static final String TAG = FirstReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg=intent.getStringExtra("msg");
        Log.d(TAG, "onReceive: "+msg);

        Bundle bundle=new Bundle();
        bundle.putString("msg",msg+"@FirstReceiver");
        setResultExtras(bundle);

        abortBroadcast();
    }
}

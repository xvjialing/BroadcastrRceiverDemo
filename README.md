>Broadcast Receiver是广播接收者的意思，用来接收来自系统的广播信息。比如android系统在完成开机后会产生
一条广播，接收到这条广播就能实现开机启动的服务；当系统电量发生改变时，系统会产生一条广播，接收这条广播
就能在电量低时告知用户及时保存进度，等等。

### 创建BroadcastReceiver
1. 继承Broadcast Receiver对象

```
public class MyReceiver extends BroadcastReceiver {
    private static final String TAG=MyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg=intent.getStringExtra("msg");
        Log.d(TAG, "onReceive: "+msg);
    }
}
```

### 为Broadcast Receiver指定广播地址
创建完Broadcast Receiver之后还无法工作，需为其指定广播地址才能接收到特定广播信息

1. 静态注册

静态注册是在AndroidManifest.xml中完成的

```
<receiver
    android:name=".MyReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MY_BROADCAST"/>
        <category android:name="android.intent.category.DEFAULT"/>
    </intent-filter>
</receiver>
```

2. 动态注册

动态注册需要我们在代码中动态地指定广播地址并注册，通常我们在Activity和Service中动态注册
```
private void registerBroadcastReceiver() {
    MyReceiver myReceiver=new MyReceiver();
    IntentFilter intentFilter=new IntentFilter();
    intentFilter.addAction("android.intent.action.MY_BROADCAST");

    registerReceiver(myReceiver,intentFilter);

}
```

当我们在Activity或Service中注册Broadcast Receiver后，当这个Activity或Service被销毁时如
果没有解除注册，系统就会报异常，提示我们是否解除注册。因此，我们动态注册完后应在特定的地方解除注册。
```
@Override
protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(myReceiver);
}
```

执行这样的代码后，问题就解决了。动态注册与静态注册不同，不是常驻型的，它的生命周期会跟随程序的
生命周期，也就是注册广播的Activity或Service的生命周期。

我们根据以上任何一种方法完成注册后,这个接收者就能工作了，下面我们用一下方式发送一条广播。
```
private void send(){
    Intent intent=new Intent("android.intent.action.MY_BROADCAST");
    intent.putExtra("msg","hello receiver");
    sendBroadcast(intent);
}
```



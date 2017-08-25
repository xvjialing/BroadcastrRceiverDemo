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


以上例子已经可以接收到广播，但如果多个接收者同时注册同一广播地址，接收者之间是否会相互干扰，
是否能同时接收到广播，这就涉及到**普通广播**与**有序广播**。

### 普通广播（Normal Broadcast）

普通广播对于多个接收者来说完全是异步的，通常每个接收者无需等待就可接收到广播，接收者之间相互
不会有影响。对于这种广播，接收者无法无法终止广播，也无法阻止其他接收者接收广播。

为此定义了三个接收者
```
<receiver
    android:name=".MyReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MY_BROADCAST" />

        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
<receiver
    android:name=".SecondReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MY_BROADCAST" />

        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
<receiver
    android:name=".ThirdReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MY_BROADCAST" />

        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```

最终结果,都接收到了信息
```
08-25 09:13:48.745 2465-2465/com.lytech.xvjialing.broadcastreceiverdemo D/MyReceiver: onReceive: hello receiver
08-25 09:13:48.762 2465-2465/com.lytech.xvjialing.broadcastreceiverdemo D/SecondReceiver: onReceive: hello receiver
08-25 09:13:48.773 2465-2465/com.lytech.xvjialing.broadcastreceiverdemo D/ThirdReceiver: onReceive: hello receiver
```

在onReceive()中添加一下代码，试图终止广播
```
abortBroadcast();
```

再次点击按钮，三者依然接收到信息，但出现以下异常信息
```
BroadcastReceiver trying to return result during a non-ordered broadcast
java.lang.RuntimeException: BroadcastReceiver trying to return result during a non-ordered broadcast
     at android.content.BroadcastReceiver.checkSynchronousHint(BroadcastReceiver.java:785)
     at android.content.BroadcastReceiver.abortBroadcast(BroadcastReceiver.java:691)
     at com.lytech.xvjialing.broadcastreceiverdemo.MyReceiver.onReceive(MyReceiver.java:15)
     at android.app.ActivityThread.handleReceiver(ActivityThread.java:3011)
     at android.app.ActivityThread.-wrap18(ActivityThread.java)
     at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1544)
     at android.os.Handler.dispatchMessage(Handler.java:102)
     at android.os.Looper.loop(Looper.java:154)
     at android.app.ActivityThread.main(ActivityThread.java:6077)
     at java.lang.reflect.Method.invoke(Native Method)
     at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:866)
     at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:756)
```

最后查到的结果是，在发送无序广播时试图终止广播就会产生此异常

### 有序广播（Ordered Broadcast）

有序广播比较特殊，它会优先将信息发送到优先级较高的接收者中，然后由优先级较高的接收者传播到优先级较低的
接收者中，优先级较高的接收者有权终止广播。

先定义三个接收者

```
public class FirstReceiver extends BroadcastReceiver {

    private static final String TAG = FirstReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg=intent.getStringExtra("msg");
        Log.d(TAG, "onReceive: "+msg);

        Bundle bundle=new Bundle();
        bundle.putString("msg",msg+"@FirstReceiver");
        setResultExtras(bundle);
    }
}
```

```
public class SecondReceiver extends BroadcastReceiver {

    private static final String TAG = SecondReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg=getResultExtras(true).getString("msg");
        Log.d(TAG, "onReceive: "+msg);

        Bundle bundle=new Bundle();
        bundle.putString("msg",msg+"@secondReceiver");
        setResultExtras(bundle);
    }
}
```

```
public class ThirdReceiver extends BroadcastReceiver {

    private static final String TAG = ThirdReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg=getResultExtras(true).getString("msg");
        Log.d(TAG, "onReceive: "+msg);
        
    }
}
```

在FirstReceiver和SecondReceiver中最后都使用了setResultExtras方法将一个Bundle对象设置为结果集对象，
传递到下一个接收者那里，这样以来，优先级低的接收者可以用getResultExtras获取到最新的经过处理的信息集合。

代码改完之后，我们需要为三个接收者注册广播地址，我们修改一下AndroidMainfest.xml文件：

```
<receiver
    android:name=".FirstReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter android:priority="1000">
        <action android:name="android.intent.action.MY_BROADCAST" />

        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
<receiver
    android:name=".SecondReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter android:priority="999">
        <action android:name="android.intent.action.MY_BROADCAST" />

        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
<receiver
    android:name=".ThirdReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter android:priority="998">
        <action android:name="android.intent.action.MY_BROADCAST" />

        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```

现在这三个接收者的<intent-filter>多了一个android:priority属性，并且依次减小。这个属性的范围在-1000到1000，数值越大，优先级越高。

发送有序广播的代码，如下：

```
private void sendOrderedMsg(){
    Intent intent=new Intent("android.intent.action.MY_BROADCAST");
    intent.putExtra("msg","hello receiver");
    sendOrderedBroadcast(intent,"xjl.permission.MY_BROADCAST_PERMISSION");
}
```

注意，使用sendOrderedBroadcast方法发送有序广播时，需要一个权限参数，如果为null则表示不要求接收者声明指定的权限，如果不为null，则表示接收者若要接收此广播，需声明指定权限。这样做是从安全角度考虑的，例如系统的短信就是有序广播的形式，一个应用可能是具有拦截垃圾短信的功能，当短信到来时它可以先接受到短信广播，必要时终止广播传递，这样的软件就必须声明接收短信的权限。

所以我们在AndroidMainfest.xml中定义一个权限：

```
<permission android:protectionLevel="normal" android:name="xjl.permission.MY_BROADCAST_PERMISSION"/>
```

然后声明使用此权限

```
<uses-permission android:name="xjl.permission.MY_BROADCAST_PERMISSION"/>
```

然后我们点击发送按钮发送一条有序广播，控制台打印如下
```
08-25 10:22:33.757 27396-27396/com.lytech.xvjialing.broadcastreceiverdemo D/FirstReceiver: onReceive: hello receiver
08-25 10:22:33.760 27396-27396/com.lytech.xvjialing.broadcastreceiverdemo D/SecondReceiver: onReceive: hello receiver@FirstReceiver
08-25 10:22:33.762 27396-27396/com.lytech.xvjialing.broadcastreceiverdemo D/ThirdReceiver: onReceive: hello receiver@FirstReceiver@secondReceiver
```

我们看到接收是按照顺序的，第一个和第二个都在结果集中加入了自己的标记，并且向优先级低的接收者传递下去。

既然是顺序传递，试着终止这种传递，看一看效果如何，我们修改FirstReceiver的代码，在onReceive的最后一行添加以下代码：
```
abortBroadcast(); 
```

```
08-25 10:28:17.023 27396-27396/com.lytech.xvjialing.broadcastreceiverdemo D/FirstReceiver: onReceive: hello receiver
```

此次，只有第一个接收者执行了，其它两个都没能执行，因为广播被第一个接收者终止了。


下面做一个开机启动的例子

#### 开机启动服务

我们经常会有这样的应用场合，比如消息推送服务，需要实现开机启动的功能。要实现这个功能，我们就可以订阅系统“启动完成”这条广播，接收到这条广播后我们就可以启动自己的服务了。我们来看一下BootCompleteReceiver和MsgPushService的具体实现：

```
public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = BootCompleteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service=new Intent(context,MsgPushService.class);
        context.startService(service);
        Log.d(TAG, "Boot Complete. Starting MsgPushService...");
    }
}
```

```
public class MsgPushService extends Service {
    private static final String TAG = MsgPushService.class.getSimpleName();

    public MsgPushService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
```

然后我们需要在AndroidManifest.xml中配置相关信息：

```
<!--开机广播接收者-->
<receiver
    android:name=".BootCompleteReceiver"
    android:enabled="true"
    android:exported="true" >

    <intent-filter>
        <!--开机广播地址-->
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
        <category android:name="android.intent.category.DEFAULT"/>
    </intent-filter>
</receiver>

<!--消息推送服务-->
<service
    android:name=".MsgPushService"
    android:enabled="true"
    android:exported="true"></service>
```

我们看到BootCompleteReceiver注册了“android.intent.action.BOOT_COMPLETED”这个开机广播地址，从安全角度考虑，系统要求必须声明接收开机启动广播的权限，于是我们再声明使用下面的权限：

```
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```

经过上面的几个步骤之后，我们就完成了开机启动的功能，将应用运行在模拟器上，然后重启模拟器，控制台打印如下：

```
08-25 10:41:10.518 2342-2342/? D/BootCompleteReceiver: Boot Complete. Starting MsgPushService...
08-25 10:41:10.521 2342-2342/? D/MsgPushService: onCreate: 
08-25 10:41:10.521 2342-2342/? D/MsgPushService: onStartCommand: 
```

参考于[这篇文章](http://blog.csdn.net/liuhe688/article/details/6955668)

[源码地址](https://github.com/xvjialing/BroadcastrRceiverDemo)


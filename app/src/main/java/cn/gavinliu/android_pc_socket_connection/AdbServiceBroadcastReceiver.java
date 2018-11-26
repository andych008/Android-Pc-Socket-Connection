package cn.gavinliu.android_pc_socket_connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by shq on 2017/5/17.
 * 监听pc端发送的adb命令开启android端服务
 */
public class AdbServiceBroadcastReceiver extends BroadcastReceiver {

    public static final String START_ACTION = "NotifyServiceStart";
    public static final String STOP_ACTION = "NotifyServiceStop";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (START_ACTION.equalsIgnoreCase(action)) {
            context.startService(new Intent(context, AdbService.class));
            Log.e(AdbService.TAG, Thread.currentThread().getName() + "------>"
                + "onReceive start");
        } else if (STOP_ACTION.equalsIgnoreCase(action)) {
            context.stopService(new Intent(context, AdbService.class));
            Log.e(AdbService.TAG, Thread.currentThread().getName() + "------>"
                + "onReceive stop");
        }
    }
}

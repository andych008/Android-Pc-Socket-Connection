package cn.gavinliu.android_pc_socket_connection;

import static cn.gavinliu.android_pc_socket_connection.AdbServiceBroadcastReceiver.START_ACTION;
import static cn.gavinliu.android_pc_socket_connection.AdbServiceBroadcastReceiver.STOP_ACTION;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start_server).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startServer();
            }
        });
        findViewById(R.id.stop_server).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                stopServer();
            }
        });
    }


    private void startServer() {
        Intent intent = new Intent();
        intent.setAction(START_ACTION);
        sendBroadcast(intent);
    }

    private void stopServer() {
        Intent intent = new Intent();
        intent.setAction(STOP_ACTION);
        sendBroadcast(intent);
    }
}
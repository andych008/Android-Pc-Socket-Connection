package cn.gavinliu.android_pc_socket_connection;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by shq on 2017/5/17.
 * 这里是监听pc端传输数据的端口
 */
public class AdbService extends Service {

    public static final String TAG = "AdbService";
    private final static int PORT = 9000;
    private ServerSocket mServerSocket = null;
    private ServerThread serverThread;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "AdbService--onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "AdbService--onStartCommand()");
        startServer();
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭线程
        stopServer();
        mainHandler.removeCallbacksAndMessages(null);
        Log.e(TAG, Thread.currentThread().getName() + "--onDestroy()");
    }


    private void startServer() {
        if (mServerSocket != null && !mServerSocket.isClosed()) {
            Log.e(TAG, Thread.currentThread().getName() + "--Server is already running");
            return;
        }
        try {
            mServerSocket = new ServerSocket(PORT);
            serverThread = new ServerThread(mServerSocket);
            serverThread.setListener(new ServerThread.Listener() {
                @Override
                public void onReceive(String text) {
                    dispToast(text);
                }
            });
            serverThread.start();

            dispToast("Server is started.");
        } catch (IOException e) {
            Log.e(TAG, "startServer err", e);
        }
    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.killIt();
        }
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                mServerSocket = null;
            } catch (IOException e) {
                Log.e(TAG, "stopServer err", e);
            }
        }
    }


    private void dispToast(final String text) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AdbService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }


    static class ServerThread extends Thread {

        private final ServerSocket mServerSocket;
        private Socket mSocket;
        private volatile boolean mRunning = true;
        private char[] buf = new char[1024];
        private ServerThread.Listener listener;


        interface Listener {

            void onReceive(String text);
        }

        public ServerThread(ServerSocket mServerSocket) {
            this.mServerSocket = mServerSocket;
        }

        public void setListener(ServerThread.Listener listener) {
            this.listener = listener;
        }

        public void killIt() {
            mRunning = false;
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "mSocket.close() err", e);
            }
        }

        @Override
        public void run() {
            while (mRunning) {
                try {
                    Log.i(TAG, "ServerThread: waiting to connect...");
                    mSocket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket err", e);
                }

                InputStream in;
                OutputStream out;
                while (mRunning) {
                    try {
                        in = mSocket.getInputStream();
                        out = mSocket.getOutputStream();
                    } catch (IOException e) {
                        Log.e(TAG, "getXXXStream err", e);
                        break;
                    }

                    InputStreamReader reader = new InputStreamReader(in);
                    try {
                        Log.i(TAG, "ServerThread: waiting to read...");
//                            // 模拟耗时
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        int cnt = reader.read(buf);
                        if (cnt > 0) {
                            final String msg = new String(buf, 0, cnt);
                            Log.i(TAG, "Receive: " + msg);
                            if (listener != null) {
                                listener.onReceive(msg);
                            }

                            String reply = "Server Said: I received '" + msg + "'";
                            out.write(reply.getBytes());
                            out.flush();
                        } else {
                            Log.i(TAG, "client exit");
                            break;
                        }
                    } catch (SocketException e) {
                        if (mRunning) {
                            Log.e(TAG, "SocketException", e);
                        } else {
                            Log.i(TAG, "Socket closed");
                            break;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "read write err", e);
                    }
                }
            }

            Log.i(TAG, "ServerThread: end");
        }

        private int read(InputStreamReader reader, StringBuilder sb) throws IOException {
            int len = 0;
            int cnt;
            while ((cnt = reader.read(buf)) != -1) {
                Log.i(TAG, "cnt = " + cnt);
                len += cnt;
                sb.append(buf, 0, cnt);
            }
            if (len <= 0) {
                Log.i(TAG, "client closed");
            }
            return len;
        }
    }
}
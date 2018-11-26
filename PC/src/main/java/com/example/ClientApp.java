package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 *
 * @author 喵叔catuncle    11/23/18
 */
public class ClientApp {

    static Socket socket = null;
    private static boolean mRunning = false;

    public static void main(String[] args) {
        try {
            // adb 指令
            Runtime.getRuntime().exec("adb forward tcp:8000 tcp:9000"); // 端口转换
            Thread.sleep(1500);
            // 启动app, 广播才能被接收
            Runtime.getRuntime().exec("adb shell am start -n cn.gavinliu.android_pc_socket_connection/.MainActivity");
            Thread.sleep(500);
            // 发送广播，启动socket服务
            Runtime.getRuntime().exec("adb shell am broadcast -a NotifyServiceStart");
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //启动client，并连接server
        startClient();
    }

    private static void startClient() {
        try {
            socket = new Socket("127.0.0.1", 8000);
            mRunning = true;
            new Thread(new InThread(socket)).start();
            new Thread(new OutThread(socket)).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class InThread implements Runnable {
        Socket socket = null;

        public InThread(Socket s) {
            socket = s;
        }

        @Override
        public void run() {
            while (mRunning) {
                if (socket.isClosed()) {
                    mRunning = false;
                    break;
                }
                InputStream in = null;
                try {
                    in = socket.getInputStream();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                InputStreamReader reader = new InputStreamReader(in);
                try {
                    char[] buf = new char[1024000];
                    int cnt = reader.read(buf);
                    if (cnt > 0) {
                        String msg = new String(buf, 0, cnt);
                        System.out.println(msg);
                    } else {
                        System.out.println("server exit");
                        System.exit(0);
                    }
                } catch (IOException e) {
                    String error = e.getMessage();
                    if (!error.equalsIgnoreCase("Socket closed")) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("InThread exit");
        }
    }

    static class OutThread implements Runnable {
        Socket socket = null;

        public OutThread(Socket s) {
            socket = s;
        }

        @Override
        public void run() {
            System.out.println("Please type in something...");
            System.out.println("Type in 'exit' to exit.");
            System.out.println();
            while (mRunning) {
                if (socket.isClosed()) {
                    mRunning = false;
                    break;
                }

                OutputStream out = null;
                try {
                    out = socket.getOutputStream();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }

                InputStreamReader consoleReader = new InputStreamReader(System.in);
                String msg = "";
                try {
                    char[] buf = new char[10240];
                    int cnt = consoleReader.read(buf);
                    if (cnt > 0) {
                        msg = new String(buf, 0, cnt);
                    } else {
                        break;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                msg = msg.trim();// delete trailing linefeed
                if (msg.equalsIgnoreCase("exit")) {
                    mRunning = false;
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                /*
                 * input "getattacksurface package_name", get the attack surface of that package
                 */
                try {
                    out.write(msg.getBytes());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("OutThread exit");
        }
    }
}
package com.huari.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.List;

import struct.JavaStruct;
import com.huari.Fragment.LineFragment;
import com.huari.commandstruct.FunctionFrame;
import com.huari.dataentry.GlobalData;
import com.huari.dataentry.SimpleStation;
import com.huari.tools.FileOsImpl;
import com.huari.tools.Parse;
import com.huari.tools.SysApplication;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {

    static Socket socket;
    static OutputStream ops;
    static InputStream ins;
    static boolean will = true, have = false;

    public static void send(byte[] b) {
        Thread thread = new Thread(() -> {
            try {
                if (ops == null) {
                    System.out.println("OPS是空的");
                }
                if (b == null) {
                    System.out.println("命令是空的");
                }
                ops.write(b);
                ops.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void stopFunction() {
        try {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startFunction() {
        int attemp = 3;
        while (socket == null && attemp > 0) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(GlobalData.mainIP, GlobalData.port1),3000);
                socket.setSoTimeout(5000);
                socket.setKeepAlive(true);
                socket.setReceiveBufferSize(655360);
                ins = socket.getInputStream();
                ops = socket.getOutputStream();
                LineFragment.handler.sendEmptyMessage(LineFragment.LINKSUCCESS);
            } catch (Exception e) {
                try {
                    Thread.sleep(300);
                }catch (Exception e2){
                    e2.printStackTrace();
                }
                attemp = attemp - 1;
                if (attemp == 0)
                    LineFragment.handler.sendEmptyMessage(LineFragment.LINKFAILED);
                socket = null;
                continue;
            }
            GlobalData.toCreatService = true;
            Thread thread = new Thread(() -> {
                try {
                    FunctionFrame ff = new FunctionFrame();
                    ff.length = 1;
                    ff.functionNum = 1;
                    byte[] b;
                    b = JavaStruct.pack(ff);
                    ops.write(b);
                    ops.flush();
                    System.out.println("第一段已发送完");

                    Thread.sleep(500);
                    FunctionFrame f1 = new FunctionFrame();
                    f1.length = 1;
                    f1.functionNum = 13;
                    byte[] b1 = JavaStruct.pack(f1);
                    ops.write(b1);
                    ops.flush();
                    new ReceiveData().start();
                } catch (Exception e) {
                    System.out.println("第一段已发送完");
                }
            });
            thread.start();

        }
        SysApplication.SocketFlag = true;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class ReceiveData extends Thread {

        public void run() {
            while (true) {
                try {
                    Thread.sleep(2);
                    int available = ins.available();
                    if (available > 4) {
                        byte[] b = new byte[available];
                        ins.read(b);
                        ParseDeviceSettingInfo();//将本地化的设置信息加载进来，和读取的台站信息进行合并，设置保存的生效之地
                        Parse.parseReceiveInfo(b);
                    }
                    try {
//                        ops.write(1);
//                        ops.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("xiaofa", "错误");
                    }
                } catch (Exception e) {
                    System.out.println("接收初始数据时异常");
                }
            }
        }

        private void ParseDeviceSettingInfo() {
            try {
                File file = new File(FileOsImpl.forSaveFloder + File.separator + "data" + File.separator + "ForSaveStation");
                if (!(file.exists())) {
                    return;
                }
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                FileOsImpl.simpleStations = (List<SimpleStation>) objectInputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("xiao", e.toString());
            }
        }
    }

}

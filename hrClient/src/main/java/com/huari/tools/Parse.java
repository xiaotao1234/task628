package com.huari.tools;

//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.media.AudioTrack;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.huari.Base.AnalysisBase;
import com.huari.Base.PinDuanBase;
import com.huari.Base.MscanBase;
import com.huari.Fragment.StationShowFragment;
import com.huari.client.DzActivity;
import com.huari.client.MScanActivity;
import com.huari.client.MapActivity;
import com.huari.client.PinDuanScanningActivity;
import com.huari.client.SinglefrequencyDFActivity;
import com.huari.client.SpectrumsAnalysisActivity;
import com.huari.client.StationListActivity;
import com.huari.client.UnmanedStationsActivity;
import com.huari.dataentry.*;

import static com.huari.dataentry.GlobalData.*;

public class Parse {

    static DecimalFormat df = new DecimalFormat("0.0000");


    public static void parseReceiveInfo(byte[] data) {
        try {
            int available = data.length;
            int index = 0;
            int fullframelength = 0;
            System.out.println("收到的长度是" + available);

            while (index < available - 3) {
                if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data, index,
                        index + 3)) == 0xEEEEEEEE
                        && infoBuffer == null)// 如果是帧头,并且缓存为Null，则证明是一个全新的开始。
                {
                    int templength = MyTools.fourBytesToInt(MyTools
                            .nigetPartByteArray(data, index + 4, index + 7));// 具体帧的长度
                    fullframelength = templength + 8;
                    System.out.println("全帧的长度是" + fullframelength);
                    if (available - index >= fullframelength)// 说明当前帧是完整的
                    {
                        byte[] temp = new byte[fullframelength];
                        System.arraycopy(data, index, temp, 0, fullframelength);
                        System.out.println("当前帧完整了，开始解析");
                        parseAllInfo(temp);
                    } else {// 当前帧是不完整的
                        infoBuffer = new byte[fullframelength];
                        System.arraycopy(data, index, infoBuffer, 0,
                                available - index);
                        infoIndex = available - index;
                        System.out.println("当前帧不完整，先缓存起来");
                    }
                    index = index + fullframelength;
                } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data,
                        index, index + 3)) != 0xEEEEEEEE
                        && infoBuffer != null)// 如果不是帧头并且GlobalData.buffer不为空，说明是前一帧的后续
                {
                    int templength = Math.min(infoBuffer.length
                            - infoIndex, data.length);
                    System.arraycopy(data, 0, infoBuffer,
                            infoIndex, templength);
                    infoIndex = infoIndex + templength;
                    System.out.println("缓存的数组长度是"
                            + infoBuffer.length + "索引是"
                            + infoIndex);

                    if (infoIndex == infoBuffer.length) {
                        Parse.parseAllInfo(infoBuffer);
                        infoBuffer = null;
                        infoIndex = 0;
                        System.out.println("当前帧凑完整了，开始解析吧！");
                    }
                    System.out.println("前一帧的后续");
                    index = index + templength;
                } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data,
                        index, index + 3)) != 0xEEEEEEEE
                        && buffer == null)// 如果不是帧头并且GlobalData.buffer为空，说明有漏帧,需要抛弃部分数据
                {
                    int m = 0;
                    for (m = 0; m < available - 3; m++) {
                        if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(
                                data, m, m + 3)) == 0xEEEEEEEE) {
                            break;
                        }
                    }
                    m = m - 1;
                    byte[] regment = new byte[data.length - m];
                    System.arraycopy(data, m, regment, 0, data.length - m);
                    index = available - 1;
                    parseReceiveInfo(regment);
                }
            }
            System.gc();
        } catch (Exception e) {
            System.out.println("parseReceiveInfo解析中出现异常");
        }

    }

    public static void parseAllInfo(byte[] info) {
        try {
            switch (info[8]) {
                case 6:// 监测站
                    // System.out.println("收到来自服务端的监测站数据");
                    //String s = null;
                    Parse.parseMonitoringStation(info);
                    if (StationListActivity.handler != null) {
                        StationListActivity.handler
                                .sendEmptyMessage(StationListActivity.REFRESHSTATE);
                    }
                    break;
                case 60:// 无人站
                    newParseUnManServer(info);
                    break;
                case 61:// 无人站更新
                    Parse.refreshUnManStation(info);
                    if (DzActivity.handler != null)
                        DzActivity.handler.sendEmptyMessage(0x9);
                    break;
                case 48:// 频谱分析（单频测量）
                    Parse.parseSpectrumsAnalysis(info);
                    if (SpectrumsAnalysisActivity.handle != null)
                        SpectrumsAnalysisActivity.handle.sendEmptyMessage(0x987);
                    break;
                case 47:// 频段扫描
                    Parse.parsePDScan(info);
                    if (PinDuanScanningActivity.handler != null)
                        PinDuanScanningActivity.handler.sendEmptyMessage(0x2);
                    break;
                case 5:// 设备状态更新
                    Parse.refreshDevice(info);
                    break;
                case 3:// 下线
                    Parse.stationDown(info);
                    if (StationListActivity.handler != null)
                        StationListActivity.handler
                                .sendEmptyMessage(StationListActivity.REFRESHSTATE);
                    if (MapActivity.handler != null) {
                        MapActivity.handler
                                .sendEmptyMessage(MapActivity.STATIONREFRESH);
                    }
                    break;
                case 4:// 上线
                    Parse.parseMonitoringStation(info);
                    if (StationListActivity.handler != null)
                        StationListActivity.handler.sendEmptyMessage(0x1);
                    break;
                case 46:// 单频测向
                    switch (info[14]) {
                        case 31:// 电平数据
                            DDFdianping = Parse.parseDianping(MyTools
                                    .getPartByteArray(info, 9, info.length - 1));
                            if (SinglefrequencyDFActivity.handle != null)
                                SinglefrequencyDFActivity.handle.sendEmptyMessage(0x8);
                            break;
                        case 21:// 示向度测量数据
                            Parse.parseShixiangdu(MyTools.getPartByteArray(info, 9,
                                    info.length - 1));
                            if (SinglefrequencyDFActivity.handle != null)
                                SinglefrequencyDFActivity.handle.sendEmptyMessage(0x8);
                            break;
                        case 8:// GPS数据来了

                            break;
                    }
            }
        } catch (Exception e) {
            Log.i("解析（如无人站更新）出现", "异常");
        }
    }

    public static void parseGPS(byte[] b) {
        try {
            String stationId = new String(MyTools.getPartByteArray(b, 9, 84),
                    "UTF-16LE").trim();
//			float lon = fourBytesToFloat(MyTools.nigetPartByteArray(b, 85, 88));// 经度
//			float lan = fourBytesToFloat(MyTools.nigetPartByteArray(b, 89, 92));
            stationHashMap.get(stationId);
            if (MapActivity.handler != null) {

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void parseMonitoringStation(byte[] b)// 解析台站
    {
        System.out.println("我所解析时的长度是" + b.length);
        int framelength = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b,
                4, 7)) - 1;// 减1后即后面所有台站的长度
        // System.out.println("所有台站的长度，即帧头里所标的长度减1后的值："+framelength);
        // Log.i("所有台站的长度，即帧头里所标的长度减1后的值：",framelength+"");
        int temp = 0;// 前面所有台站的累积长度
        while (framelength - temp > 0)// 大于0说明有台站
        {
            // System.out.println("总收到数据"+b.length+"字节");
            int currentDevicesLength = MyTools.nifourBytesToInt(MyTools
                    .getPartByteArray(b, temp + 242, temp + 245));// 当前台站所有设备的长度

            // System.out.println("当前台站所有设备的长度，即台站中所标的长度值："+currentDevicesLength);

            int currentStationLength = 237 + currentDevicesLength;
            byte[] current = MyTools.getPartByteArray(b, 9 + temp, 8 + temp
                    + currentStationLength);// 解析出的台站
            com.huari.dataentry.Station station = Parse.parseStation(current);// 返回一个Station对象，该对象的各个属性（除下挂设备外）已被赋值
            String currentStationId = null;
            try {
                currentStationId = new String(MyTools.getPartByteArray(current,
                        37, 112), "UTF-16LE").trim();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }// Station的ID
            // System.out.println("当前台站的名称："+station.name);
            ArrayList<com.huari.dataentry.MyDevice> devicelist = new ArrayList<com.huari.dataentry.MyDevice>();
            ArrayList<MyDevice> showdevicelist = new ArrayList<MyDevice>();
            byte[] currentAllDevice = MyTools.getPartByteArray(current, 237,
                    237 + currentDevicesLength - 1);// 该数组为当前台站所有设备
            int detemp = 0;// 前面所有设备的累积长度
            while (currentDevicesLength - detemp > 0)// 大于0说明后面有设备。开始解析设备。
            {
                try {
                    int currentLogicLength = MyTools.fourBytesToInt(MyTools
                            .nigetPartByteArray(currentAllDevice, 39 + detemp,
                                    detemp + 42));// 当前设备的所有逻辑参数的长度
                    // System.out.println("当前设备的所有逻辑数据的长度，即设备中所标的长度："+currentLogicLength);
                    int currentSingleDeviceLength = 43 + currentLogicLength;
                    byte[] linshide = MyTools.getPartByteArray(
                            currentAllDevice, detemp, detemp + 38);
                    MyDevice mydevice = Parse.parseDevice(linshide);
                    // System.out.println("当前设备的名称："+mydevice.name);
                    // System.out.println("当前设备含有逻辑数据个数："+mydevice.logicParametersCount+"当前设备状态"+mydevice.state);
                    byte[] currentSingleDevice = MyTools.getPartByteArray(
                            currentAllDevice, detemp, detemp
                                    + currentSingleDeviceLength - 1);// 当前的单个设备
                    HashMap<String, LogicParameter> lpMap = new HashMap<String, LogicParameter>();

                    byte[] currentLogic = MyTools.getPartByteArray(
                            currentSingleDevice, 43,
                            43 + currentLogicLength - 1);// 当前设备的所有逻辑参数

                    int logictemp = 0;// 前面所有逻辑参数的累积长度
                    while (currentLogicLength - logictemp > 0)// 大于0说明后面有逻辑参数。开始解析逻辑参数。
                    {
                        int currentParametersLength = MyTools
                                .fourBytesToInt(MyTools.nigetPartByteArray(
                                        currentLogic, 128 + logictemp,
                                        131 + logictemp));
                        // System.out.println("当前逻辑数据中所有参数的长度，即逻辑数据中所标的长度"+currentParametersLength);
                        int currentSingleLogicLength = 132 + currentParametersLength;// 当前所有逻辑参数的总长度
                        byte[] currentSingleLogic = MyTools.getPartByteArray(
                                currentLogic, logictemp, logictemp
                                        + currentSingleLogicLength - 1);// 当前的逻辑参数

                        LogicParameter lp = Parse
                                .parseLogic(currentSingleLogic);

                        System.out.println("逻辑数据的Type:" + lp.type);

                        ArrayList<Parameter> plist = new ArrayList<Parameter>();
                        byte[] allparameters = MyTools.getPartByteArray(
                                currentSingleLogic, 132,
                                132 + currentParametersLength - 1);// 当前逻辑参数的所有参数
                        int paratemp = 0;
                        while (currentParametersLength - paratemp > 0) {
                            int currentparaLength = MyTools
                                    .fourBytesToInt(MyTools.nigetPartByteArray(
                                            allparameters, paratemp,
                                            paratemp + 3));
                            int currentparaTrueLength = currentparaLength + 4;
                            byte[] currentsingleParameter = MyTools
                                    .getPartByteArray(allparameters, paratemp,
                                            currentparaTrueLength - 1
                                                    + paratemp);
                            try {
                                Parameter p = Parse.parseParameter(currentsingleParameter);
                                plist.add(p);
                            } catch (Exception e) {
                                System.out.println("小错误");
                            }
                            paratemp = (paratemp + currentparaTrueLength);
                        }

                        lp.parameterlist = plist;
                        lpMap.put(lp.id, lp);

                        logictemp = logictemp + currentSingleLogicLength;
                    }// 解析逻辑参数结束
                    mydevice.logic = lpMap;
                    if (mydevice.logic.size() > 0 || mydevice.state == 1)// state为1表示有故障
                    {
                        showdevicelist.add(mydevice);
                    }
                    devicelist.add(mydevice);
                    detemp = detemp + currentSingleDeviceLength;

                } catch (Exception e) {
                    System.out.println("大事不好啦！出现错误啦！");
                }
            }// 解析设备结束
            station.devicelist = devicelist;
            if (FileOsImpl.simpleStations.size() != 0) {
                showdevicelist = recoverSetOfDevice(showdevicelist, station.getId(), station.getName());
            }
            station.showdevicelist = showdevicelist;
            stationHashMap.put(currentStationId, station);
            Message message = Message.obtain();
            message.what = 0;
            StationShowFragment.handler.sendMessage(message);
            temp = currentStationLength + temp;
        }// 最大的while截止。解析台站。
        // if(GlobalData.stationHashMap!=null)
        // {
        // System.out.println("共有 "+GlobalData.stationHashMap.size()+" 个台站\n");
        // for(String s:GlobalData.stationHashMap.keySet())
        // {
        // Station stat=GlobalData.stationHashMap.get(s);
        // System.out.println("台站名称："+stat.name+"台站ID"+stat.id);
        // System.out.println("该台站有设备："+stat.devicelist.size()+" 台");
        // for(int i=0;i<stat.devicelist.size();i++)
        // {
        // MyDevice device=stat.devicelist.get(i);
        // System.out.println("设备名称： "+device.name);
        // System.out.println("该设备有逻辑数据  "+device.logicParametersCount+"  个");
        // for(String t:device.logic.keySet())
        // {
        // LogicParameter lp=device.logic.get(t);
        // System.out.println("逻辑数据id:  "+lp.id);
        // System.out.println("该逻辑数据有参数   "+lp.parameterlist.size()+" 个");
        // for(int m=0;m<lp.parameterlist.size();m++)
        // {
        // Parameter pa=lp.parameterlist.get(m);
        // System.out.println("参数名称："+pa.name+"是否高级   "+pa.isAdvanced+"  参数最大值："+pa.maxValue+
        // "  参数最小值："+pa.minValue+"  参数默认值："+pa.defaultValue+
        // "  参数显示范畴："+pa.displayType+"  枚举类型："+pa.enumValues);
        // for(String ss:pa.enumValues)
        // {
        // System.out.print(ss+"  ");
        // }
        // }
        // }
        // }
        // System.out.println("\n\n");
        // }
        // }

    }

    private static ArrayList<MyDevice> recoverSetOfDevice(ArrayList<MyDevice> devices, String stationid, String stationname) {
        for (MyDevice device : devices) {
            SimpleStation simpleStation = new SimpleStation(stationname, stationid, device);
            if (FileOsImpl.simpleStations.contains(simpleStation)) {
                devices.get(devices.indexOf(device)).logic = FileOsImpl.simpleStations.get(FileOsImpl.simpleStations.indexOf(simpleStation)).getDevice().logic;
//                devices.set(devices.indexOf(device),FileOsImpl.simpleStations.get(FileOsImpl.simpleStations.indexOf(simpleStation)).getDevice());
            }//通过比对找出其中已经被保存在本地的设备的设置，然后本地的替换到设备列表中然后再返回。
        }
        return devices;
    }

    public static void parseUnManedStation(byte[] b)// 解析无人站
    {
        int framelength = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b,
                4, 7)) - 1;// 减1后即后面所有服务器的长度
        int temp = 0;
        while (framelength - temp > 0)// 说明有服务器
        {
            int currentServerLength = MyTools.fourBytesToInt(MyTools
                    .nigetPartByteArray(b, 9 + temp, 9 + temp + 3));// 当前服务器标值的长度
            int currentServerFullLength = currentServerLength + 4;
            byte[] currentServer = MyTools.getPartByteArray(b, 9 + temp, 12
                    + temp + currentServerLength);// 当前的Server
            UnManServer ums = Parse.parseUnManServer(currentServer);
            int untemp = MyTools.twoBytesToShort(MyTools.nigetPartByteArray(
                    currentServer, 180, 181));// 当前服务器中的不定长
            HashMap<String, UnManStation> un = new HashMap<String, UnManStation>();

            int servertemp = 0;
            while (currentServerLength - 178 - untemp - servertemp > 0)// 说明后面还有无人站
            {
                int currentnumanstationlength = MyTools.fourBytesToInt(MyTools
                        .nigetPartByteArray(currentServer, 182 + untemp
                                + servertemp, 185 + untemp + servertemp));
                int currentunmanlength = currentnumanstationlength + 4;
                byte[] unmanstation = MyTools.getPartByteArray(currentServer,
                        182 + untemp + servertemp, 181 + untemp + servertemp
                                + currentunmanlength);// 当前的无人站
                UnManStation unstation = Parse.parseUnManStation(unmanstation);
                unstation.server = ums.name;
                un.put(unstation.id, unstation);
                unmanHashMap.put(unstation.id, unstation);
                servertemp = servertemp + currentunmanlength;
            }
            ums.unman = un;
            unmanServerHashMap.put(ums.id, ums);
            temp = temp + currentServerFullLength;
        }

    }

    public static MyDevice parseDevice(byte[] b) {
        MyDevice my = new MyDevice();
        try {
            my.name = new String(MyTools.getPartByteArray(b, 0, 35), "UTF-16LE")
                    .trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        my.logicParametersCount = b[36];
        my.state = b[37];
        my.isOccupied = b[38];
        return my;
    }

    public static Station parseStation(byte[] b) {
        Station station = new Station();
        station.isCenter = b[0];// 是否控制中心
        try {
            station.centerName = new String(MyTools.getPartByteArray(b, 1, 32),
                    "UTF-16LE").trim();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            station.id = new String(MyTools.getPartByteArray(b, 33, 108),
                    "UTF-16LE").trim();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            station.parentId = new String(
                    MyTools.getPartByteArray(b, 109, 184), "UTF-16LE");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            station.name = new String(MyTools.getPartByteArray(b, 185, 216),
                    "UTF-16LE").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //station.lon = fourBytesToFloat(MyTools.nigetPartByteArray(b, 225, 228));// 经度
        //station.lan = fourBytesToFloat(MyTools.nigetPartByteArray(b, 229, 232));// 纬度
        station.lon = fourBytesToFloat(MyTools.getPartByteArray(b, 225, 228));// 经度
        station.lan = fourBytesToFloat(MyTools.getPartByteArray(b, 229, 232));// 纬度

//        station.lon = Float.intBitsToFloat(Integer.valueOf("42d01732", 16));
//        station.lan = Float.intBitsToFloat(Integer.valueOf("41f14d5e", 16));
        return station;
    }

    public static LogicParameter parseLogic(byte[] b) {
        LogicParameter lp = new LogicParameter();

        try {
            lp.id = new String(MyTools.getPartByteArray(b, 0, 75), "UTF-16LE");
            String k = new String(MyTools.getPartByteArray(b, 76, 91),
                    "UTF-16LE").toUpperCase();
            lp.type = k.trim();
            lp.name = new String(MyTools.getPartByteArray(b, 92, 127),
                    "UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return lp;
    }

    public static Parameter parseParameter(byte[] b) {
        Parameter pa = new Parameter();
        try {
            pa.name = new String(MyTools.getPartByteArray(b, 4, 39), "UTF-16LE")
                    .trim();
            // System.out.println(pa.name);
            pa.maxValue = fourBytesToFloat(MyTools.getPartByteArray(b, 40, 43));
            // System.out.println(pa.maxValue);
            pa.minValue = fourBytesToFloat(MyTools.getPartByteArray(b, 44, 47));
            // System.out.println(pa.minValue);
            pa.defaultValue = new String(MyTools.getPartByteArray(b, 48, 83),
                    "UTF-16LE").trim();
            // System.out.println(pa.defaultValue);
            pa.displayType = new String(MyTools.getPartByteArray(b, 84, 99),
                    "UTF-16LE").trim();
            // System.out.println(pa.displayType);
            pa.isAdvanced = b[100];
            pa.isEditable = b[101];

            pa.dispname = new String(MyTools.getPartByteArray(b, 102, 121), "UTF-16LE")
                    .trim();
            // System.out.println(pa.dispname);

            if (b.length > 122) {
                String s = new String(MyTools.getPartByteArray(b, 122,
                        b.length - 1), "UTF-16LE").trim();
                if (pa.name.equals("CenterFreq")) {
                    System.out.println("开始吧：");
                    System.out.println("中心频率参数长度：" + b.length);
                    for (int i = 94; i < b.length - 1; i++) {
                        System.out.print("隔" + b[i] + "开");
                    }
                }
                String[] ps = s.split("\\|");
                String[] pss = null;
                if (ps[0].equals("")) {
                    pss = new String[ps.length - 1];
                    for (int i = 0; i < pss.length; i++) {
                        pss[i] = ps[i + 1];
                    }
                    pa.enumValues = pss;
                } else {
                    pa.enumValues = ps;
                }

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return pa;
    }

    public static UnManServer parseUnManServer(byte[] b) {
        UnManServer ums = new UnManServer();
        ums.name = new String(MyTools.getPartByteArray(b, 4, 39));
        ums.id = new String(MyTools.getPartByteArray(b, 40, 115));
        ums.parentId = new String(MyTools.getPartByteArray(b, 116, 191));
        short sho = twoBytesToShort(MyTools.nigetPartByteArray(b, 192, 193));
        ums.sonstationIdArray = new String(MyTools.getPartByteArray(b, 194,
                194 + sho - 1)).split("|");
        for (String ss : ums.sonstationIdArray) {
            ums.sonStationId.add(ss);
        }
        return ums;
    }

    public static UnManStation parseUnManStation(byte[] b) {
        UnManStation um = new UnManStation();
        um.id = new String(MyTools.getPartByteArray(b, 4, 79));
        um.name = new String(MyTools.getPartByteArray(b, 80, 115));
        um.lan = Parse
                .fourBytesToFloat(MyTools.nigetPartByteArray(b, 116, 123));
        um.lon = Parse
                .fourBytesToFloat(MyTools.nigetPartByteArray(b, 124, 131));
        um.isavailable = b[132];
        um.iskongtiao = b[133];
        int t = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b, 0, 3));
        um.switcharray = new String(MyTools.getPartByteArray(b, 134, 3 + t))
                .split("|");
        for (String y : um.switcharray) {
            um.switchlist.add(y);
        }
        return um;
    }

    private static GSMDecoder gsmdecoder;
    private static byte oldaudiotype = 0;
    private static byte newaudiotype = 0;
    public static void newParseSpectrumsAnalysis(byte[] src)// 最新的频谱分析
    {
        try {
            int available = src.length;
            int index = 0;
            int fullframelength = 0;

            while (index < available - 3) {
                if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(src, index,
                        index + 3)) == 0xEEEEEEEE)  // 如果是帧头
                {
                    int templength = MyTools.fourBytesToInt(MyTools
                            .nigetPartByteArray(src, index + 4, index + 7));  // 具体帧的长度
                    fullframelength = templength + 8;
                    if (available - index >= fullframelength)  // 说明当前帧是完整的
                    {
                        switch (src[index + 8]) {
                            case 31:// 电平
                                byte[] temp = new byte[]{src[23 + index],
                                        src[22 + index]};
                                dianping = (short) (Parse
                                        .twoBytesToShort(temp) / 10);
                                AnalysisBase.handle
                                        .sendEmptyMessage(AnalysisBase.DIANPINGDATA);  //0x987;
                                break;
                            case 36:// 频谱数据
                                int count = (fullframelength - 34) / 2;
                                if (Spectrumpinpu == null
                                        || oldcount != count) {
                                    Spectrumpinpu = new short[count];
                                    Spectrumavg = new short[count];
                                    Spectrummin = new short[count];
                                    Spectrummax = new short[count];
                                    Avg = new float[count];
                                    for (int i = 0; i < count; i++) {
                                        Spectrummax[i] = -2550;
                                        Spectrummin[i] = 5000;
                                    }
                                    oldcount = count;
                                }

                                int y = 0;
                                for (int n = index + 34; n < index
                                        + fullframelength - 1; n = n + 2) {
                                    Spectrumpinpu[y] = MyTools
                                            .twoBytesToShort(new byte[]{src[n + 1], src[n]}) ;
                                    if (Spectrummax[y] < Spectrumpinpu[y]) {
                                        Spectrummax[y] = Spectrumpinpu[y];
                                    }
                                    if (Spectrummin[y] > Spectrumpinpu[y]) {
                                        Spectrummin[y] = Spectrumpinpu[y];
                                    }

                                    Avg[y] = (Avg[y]
                                            * haveCount + Spectrumpinpu[y] ) / (haveCount + 1);
                                    Spectrumavg[y] =(short) Avg[y];

                                    y++;
                                }
                                haveCount++;
                                AnalysisBase.handle
                                        .sendEmptyMessage(AnalysisBase.PINPUDATA);
                                break;
                            case 32:// ITU
                                byte[] t = new byte[fullframelength - 22];
                                System.arraycopy(src, index + 22, t, 0, t.length);
                                String[] temp2 = new String(t, "UTF-16LE")
                                        .split("\\|");
                                for (int e = 0; e < temp2.length; e++) {
                                    String[] mm = temp2[e].split(",");
                                    ituHashMap.put(mm[0], mm[1]);
                                }
                                AnalysisBase.handle.sendEmptyMessage(AnalysisBase.ITUDATA);
                                break;
                            case 35:// 声音帧
                            case 39:
                                newaudiotype = src[index + 8];
//                                if (newaudiotype != audio_type)
//                                    break;
                                boolean gsm610 = false;
//                                if (newaudiotype == 39)
//                                    gsm610 = true;

                                int framelength = fullframelength - 21 ;
                                Log.i("当前帧完整", "帧长" + fullframelength + " ");
                                if (newaudiotype != oldaudiotype || isFirstAudio) {
                                    int caiyangpinlv = MyTools
                                            .fourBytesToInt(MyTools
                                                    .nigetPartByteArray(src, index + 17, index + 20));
                                    short channelcount = MyTools
                                            .twoBytesToShort(new byte[]{
                                                    src[index + 16], src[index + 15]});
                                    byte geshi = src[index + 14];

                                    if (!gsm610) {
                                        AnalysisBase.createAudioPlay(
                                                caiyangpinlv, geshi, channelcount);
                                    } else {
                                        gsmdecoder = new GSMDecoder(true);
                                        AnalysisBase.createAudioPlay(
                                                8000, geshi, channelcount);
                                    }
//                                    try{
//                                        Thread.sleep(50);
//                                    }catch (Exception e){
//                                        e.printStackTrace();
//                                    }
                                    isFirstAudio = false;
                                }
                                byte[] finalSrc = src;
                                int finalIndex = index;
                                boolean gsm = gsm610;

                                AnalysisBase.executor.execute(() -> {
                                    try {
                                        AudioScadule(finalSrc, finalIndex, framelength, false);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                                oldaudiotype = newaudiotype;
                                break;
                            case 34:// 因子
                                int l = MyTools.fourBytesToInt(MyTools
                                        .nigetPartByteArray(src, index + 30,
                                                index + 33));
                                yinzi = new short[l];
                                int g = 0;
                                for (int v = index + 34; v < index
                                        + fullframelength - 1; v = v + 2) {
                                    yinzi[g] = (short) (MyTools
                                            .twoBytesToShort(new byte[]{src[v + 1],
                                                    src[v]}) / 10);
                                    g++;
                                }
                                break;
                            case 33:// IQ
                                int c = MyTools.fourBytesToInt(MyTools
                                        .nigetPartByteArray(src, index + 23,
                                                index + 26));
                                int ui = 0;
                                int uq = 0;
                                switch (src[index + 22]) {
                                    case 0:
                                        for (int r = index + 27; r < index + c * 2 + 26; r = r + 2) {
                                            siBuffer[ui] = MyTools
                                                    .twoBytesToShort(new byte[]{
                                                            src[r + 1], src[r]});
                                            ui++;
                                        }
                                        for (int w = index + 27 + c * 2; w < index + 4
                                                * c + 26; w = w + 2) {
                                            sqBuffer[uq] = MyTools
                                                    .twoBytesToShort(new byte[]{
                                                            src[w + 1], src[w]});
                                            uq++;
                                        }
                                        break;
                                    case 1:
                                        for (int r = index + 27; r < index + 4 * c + 23; r = r + 4) {
                                            iBuffer[ui] = MyTools
                                                    .twoBytesToShort(MyTools
                                                            .nigetPartByteArray(src, r,
                                                                    r + 3));
                                            ui++;
                                        }
                                        for (int w = index + 27 + c * 4; w < index + 8
                                                * c + 23; w = w + 4) {
                                            qBuffer[uq] = MyTools
                                                    .twoBytesToShort(MyTools
                                                            .nigetPartByteArray(src, w,
                                                                    w + 3));
                                            uq++;
                                        }
                                        break;
                                }
                                break;
                        }
                    } else {// 当前帧是不完整的
                        buffer = new byte[fullframelength];
                        Log.i("当前帧不完整", "帧长" + fullframelength + " ");
                        System.arraycopy(src, index, buffer, 0,
                                available - index);
                        Log.i("不完整帧当前索引", bufferIndex + "  ");
                        bufferIndex = available - index;
                        Log.i("不完整帧当前最新索引", bufferIndex + "  ");
                    }
                    index = index + fullframelength;
                } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(src,
                        index, index + 3)) != 0xEEEEEEEE)
                // &&
                // GlobalData.buffer!=null)//如果不是帧头并且GlobalData.buffer不为空，说明是前一帧的后续
                {
                    Log.i("后续部分到了", "出发");
                    int templength = Math.min(buffer.length
                            - bufferIndex, src.length);
                    System.arraycopy(src, 0, buffer,
                            bufferIndex, templength);
                    bufferIndex = bufferIndex
                            + templength;
                    if (bufferIndex == buffer.length) {
                        Parse.newParseSpectrumsAnalysis(buffer);
                        buffer = null;
                        bufferIndex = 0;
                    }
                    index = index + templength;
                } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(src,
                        index, index + 3)) != 0xEEEEEEEE
                        && buffer == null)// 如果不是帧头并且GlobalData.buffer为空，说明有漏帧,需要抛弃部分数据
                {
                    Log.i("有漏帧", "发生");
                    int m = 0;
                    for (m = 0; m < available - 3; m++) {
                        if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(
                                src, m, m + 3)) == 0xEEEEEEEE) {
                            break;
                        }
                    }
                    m = m - 1;
                    byte[] regment = new byte[src.length - m];
                    System.arraycopy(src, m, regment, 0, src.length - m);
                    index = available - 1;
                    Parse.newParseSpectrumsAnalysis(regment);
                }
            }

            System.gc();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private static void AudioScadule(byte[] src, int index, int framelength, boolean gsm610) throws IOException {
        synchronized (AnalysisBase.synObject) {
            int leftlength = framelength;
            int costlength = 0;

//            byte[] tempBytes;
//            byte[] tmpdata = new byte[500];

//            if (gsm610){
//                /*
//                long totalAudioLen = framelength;
//                long totalDataLen =  52 + ((totalAudioLen + 1) & ~0x1);
//                tmpdata = new byte[60 + framelength];
//
//                long longSampleRate = 8000;
//                int channels = 1;
//                long byteRate = 1625;
//
//                byte[] header = new byte[60];
//                header[0] = 'R'; // RIFF/WAVE header
//                header[1] = 'I';
//                header[2] = 'F';
//                header[3] = 'F';
//                header[4] = (byte) (totalDataLen & 0xff);
//                header[5] = (byte) ((totalDataLen >> 8) & 0xff);
//                header[6] = (byte) ((totalDataLen >> 16) & 0xff);
//                header[7] = (byte) ((totalDataLen >> 24) & 0xff);
//                header[8] = 'W';
//                header[9] = 'A';
//                header[10] = 'V';
//                header[11] = 'E';
//                header[12] = 'f'; // 'fmt ' chunk
//                header[13] = 'm';
//                header[14] = 't';
//                header[15] = ' ';
//                header[16] = 20; // 4 bytes: size of 'fmt ' chunk
//                header[17] = 0;
//                header[18] = 0;
//                header[19] = 0;
//
//                header[20] = 49; // format = 1,49
//                header[21] = 0;
//
//                header[22] = (byte) channels;
//                header[23] = 0;
//
//                header[24] = (byte) (longSampleRate & 0xff);
//                header[25] = (byte) ((longSampleRate >> 8) & 0xff);
//                header[26] = (byte) ((longSampleRate >> 16) & 0xff);
//                header[27] = (byte) ((longSampleRate >> 24) & 0xff);
//
//                header[28] = (byte) (byteRate & 0xff);
//                header[29] = (byte) ((byteRate >> 8) & 0xff);
//                header[30] = (byte) ((byteRate >> 16) & 0xff);
//                header[31] = (byte) ((byteRate >> 24) & 0xff);
//
//                header[32] = 65; // block align
//                header[33] = 0;
//                header[34] = 0; // bits per sample
//                header[35] = 0;
//
//                header[36] = 2;
//                header[37] = 0;
//
//                header[38] = 64;
//                header[39] = 1;
//
//                header[40] = 'f';
//                header[41] = 'a';
//                header[42] = 'c';
//                header[43] = 't';
//
//                header[44] = 4;
//                header[45] = 0;
//                header[46] = 0;
//                header[47] = 0;
//
//                header[48] = 0;
//                header[49] = 0;
//                header[50] = 0;
//                header[51] = 0;
//
//                header[52] = 'd';
//                header[53] = 'a';
//                header[54] = 't';
//                header[55] = 'a';
//
//                header[56] = (byte) (totalAudioLen & 0xff);
//                header[57] = (byte) ((totalAudioLen >> 8) & 0xff);
//                header[58] = (byte) ((totalAudioLen >> 16) & 0xff);
//                header[59] = (byte) ((totalAudioLen >> 24) & 0xff);
//
//                System.arraycopy(header,0,tmpdata,0,60);
//                System.arraycopy(src,index+21, tmpdata,60, framelength);
//                 */
//
//                AssetManager assetManager= SysApplication.getContext().getAssets();
//                //context.getClass().getClassLoader().getResourceAsStream("assets/ding.wav");
//                try {
//                    AudioFormat pcmFormat = new AudioFormat(8000f,16,1,false,true);
//                    // 把 传过来的音频数据(gsm)转化成 inputStream
//                    InputStream inputStream = assetManager.open("DINGGSM.WAV");
//                            //new ByteArrayInputStream(tmpdata);
//
//                    // 把 inputStream 转化对应的音频输入流 audioInputStream
//                    AudioInputStream gsmInputStream = AudioSystem.getAudioInputStream(inputStream);
//                    // 在根据pcmFormat 转化成 pcm 格式的 inputStream
//                    AudioInputStream pcmInputStream = AudioSystem.getAudioInputStream(pcmFormat, gsmInputStream);
//
//                    int len;
//                    len = (int)pcmInputStream.getFrameLength();
//                    tempBytes = new byte[len];
//
//                    pcmInputStream.read(tempBytes);
//                    leftlength = len;
//                } catch (UnsupportedAudioFileException | IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//
//                if (AnalysisBase.tempbufferindex > 0)     // 说明缓存里有数据，
//                {
//                    System.arraycopy(
//                            AnalysisBase.tempAudioBuffer,
//                            0,
//                            AnalysisBase.audioBuffer,
//                            0,
//                            AnalysisBase.tempbufferindex);
//                    AnalysisBase.audioindex += AnalysisBase.tempbufferindex;
//                    AnalysisBase.tempbufferindex = 0;
//                }
//
//                if (leftlength < AnalysisBase.audioBuffersize) {
//                    int minLength = Math
//                            .min(leftlength,
//                                    AnalysisBase.audioBuffersize
//                                            - AnalysisBase.audioindex);
//
//                    System.arraycopy(tempBytes, 0,
//                            AnalysisBase.audioBuffer,
//                            AnalysisBase.audioindex,
//                            minLength);
//
//                    AnalysisBase.audioindex += minLength;
//
//                    if (AnalysisBase.audioindex >= AnalysisBase.audioBuffersize) {
//                        AnalysisBase.at.write(AnalysisBase.audioBuffer, 0,
//                                AnalysisBase.audioBuffersize);     //播放一帧音频数据
//                        if (AnalysisBase.isRecording) {
//                            AnalysisBase.fos.write(AnalysisBase.audioBuffer);//保存一帧音频数据
//                        }
//                        AnalysisBase.audioindex = 0;
//                    }
//
//                    leftlength -= minLength;
//                    costlength += minLength;
//                } else {
//                    int anum = leftlength / AnalysisBase.audioBuffersize;
//                    while (anum > 0) {
//                        int minLength = Math.min(leftlength,
//                                AnalysisBase.audioBuffersize
//                                        - AnalysisBase.audioindex);
//
//                        System.arraycopy(tempBytes,  costlength,
//                                AnalysisBase.audioBuffer,
//                                AnalysisBase.audioindex,
//                                minLength);
//
//                        AnalysisBase.audioindex += minLength;
//
//                        if (AnalysisBase.audioindex >= AnalysisBase.audioBuffersize) {
//                            AnalysisBase.at.write(AnalysisBase.audioBuffer, 0,
//                                    AnalysisBase.audioBuffersize);
//                            if (AnalysisBase.isRecording) {
//                                AnalysisBase.fos.write(AnalysisBase.audioBuffer);
//                            }
//                            AnalysisBase.audioindex = 0;
//                        }
//
//                        leftlength -= minLength;
//                        costlength += minLength;
//
//                        anum = leftlength / AnalysisBase.audioBuffersize;
//                    }
//                }
//
//                if (leftlength > 0)         // 来的数据有剩余，则缓存起来
//                {
//                    System.arraycopy(
//                            tempBytes,
//                            costlength,
//                            AnalysisBase.tempAudioBuffer,
//                            0, leftlength);
//                    AnalysisBase.tempbufferindex = leftlength;
//                }
//            }
            if (gsm610) {
                byte[] gsm_bytes = new byte[framelength];
                System.arraycopy(src, index + 21,
                        gsm_bytes, 0, framelength);

                ByteArrayInputStream bytes_ins = new ByteArrayInputStream(gsm_bytes);

                try {
                    gsmdecoder.process_frame(bytes_ins);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                leftlength = gsmdecoder.temp_audiolength;
            }
            else
            {
                if (AnalysisBase.tempbufferindex > 0)     // 说明缓存里有数据，
                {
                    System.arraycopy(
                            AnalysisBase.tempAudioBuffer,
                            0,
                            AnalysisBase.audioBuffer,
                            0,
                            AnalysisBase.tempbufferindex);
                    AnalysisBase.audioindex += AnalysisBase.tempbufferindex;
                    AnalysisBase.tempbufferindex = 0;
                }

                if (leftlength < AnalysisBase.audioBuffersize) {
                    int minLength = Math
                            .min(leftlength,
                                    AnalysisBase.audioBuffersize - AnalysisBase.audioindex);

                    if (!gsm610)
                        System.arraycopy(src, index + 21,
                                AnalysisBase.audioBuffer,
                                AnalysisBase.audioindex,
                                minLength);
                    else
                        System.arraycopy(gsmdecoder.temp_audiodata, 0,
                                AnalysisBase.audioBuffer,
                                AnalysisBase.audioindex,
                                minLength);

                    AnalysisBase.audioindex += minLength;

                    if (AnalysisBase.audioindex >= AnalysisBase.audioBuffersize) {
                        AnalysisBase.at.write(AnalysisBase.audioBuffer, 0,
                                AnalysisBase.audioBuffersize);
                        if (AnalysisBase.isRecording) {
                            AnalysisBase.fos.write(AnalysisBase.audioBuffer);
                        }

                        AnalysisBase.audioindex = 0;
                    }

                    leftlength -= minLength;
                    costlength += minLength;
                } else {
                    int anum = leftlength / AnalysisBase.audioBuffersize;
                    while (anum > 0) {
                        int minLength = Math.min(leftlength,
                                AnalysisBase.audioBuffersize - AnalysisBase.audioindex);

                        if (!gsm610)
                            System.arraycopy(src, index + 21 + costlength,
                                    AnalysisBase.audioBuffer,
                                    AnalysisBase.audioindex,
                                    minLength);
                        else
                            System.arraycopy(gsmdecoder.temp_audiodata, 0 + costlength,
                                    AnalysisBase.audioBuffer,
                                    AnalysisBase.audioindex,
                                    minLength);

                        AnalysisBase.audioindex += minLength;

                        if (AnalysisBase.audioindex >= AnalysisBase.audioBuffersize) {
                            AnalysisBase.at.write(AnalysisBase.audioBuffer, 0,
                                    AnalysisBase.audioBuffersize);
                            if (AnalysisBase.isRecording) {
                                AnalysisBase.fos.write(AnalysisBase.audioBuffer);
                            }
                            AnalysisBase.audioindex = 0;
                        }

                        leftlength -= minLength;
                        costlength += minLength;

                        anum = leftlength / AnalysisBase.audioBuffersize;
                    }
                }

                if (leftlength > 0)         // 来的数据有剩余，则缓存起来
                {
                    if (!gsm610)
                        System.arraycopy(
                                src,
                                index + 21 + costlength,
                                AnalysisBase.tempAudioBuffer,
                                0, leftlength);
                    else
                        System.arraycopy(
                                gsmdecoder.temp_audiodata,
                                costlength,
                                AnalysisBase.tempAudioBuffer,
                                0, leftlength);

                    AnalysisBase.tempbufferindex = leftlength;
                }
            }
        }
    }

    public static void parseSpectrumsAnalysis(byte[] b) {
        int alllength = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b, 4,
                7));
        int availablelength = 0;
        int iTUlength = 0;
        int pinpulength = 0;
        byte[] pinpu;
        byte[] dianping;

        if (alllength > 1) {
            availablelength = MyTools.fourBytesToInt(MyTools
                    .nigetPartByteArray(b, 9, 12));
            iTUlength = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b,
                    13, 16));
        }
        if (availablelength > 0) {
        }
        if (availablelength - 4 - iTUlength > 0) {
            pinpulength = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b,
                    17 + iTUlength, 20 + iTUlength));
            pinpu = MyTools.getPartByteArray(b, 17 + iTUlength, 20 + iTUlength
                    + pinpulength);
            parsePinPu(pinpu);
        }
        if (availablelength - 4 - iTUlength - 4 - pinpulength > 0) {
            dianping = MyTools.getPartByteArray(b,
                    21 + pinpulength + iTUlength, 12 + availablelength);
            GlobalData.dianping = parseDianping(dianping);
        }
    }

    public static void parseITU(byte[] b)// 解析ITU
    {
//		int length = MyTools
//				.fourBytesToInt(MyTools.nigetPartByteArray(b, 0, 3));
//		byte type = b[4];
//		double d = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b, 5, 12));
        byte[] temp = MyTools.getPartByteArray(b, 13, b.length - 1);
        String[] tempString = new String(temp).split("|");
        HashMap<String, String> map = new HashMap<>();
        for (String t : tempString) {
            String[] t2 = t.split(",");
            map.put(t2[0], t2[1]);
        }
    }

    public static void parsePinPu(byte[] b)// 解析频谱数据帧
    {
//		int length = MyTools
//				.fourBytesToInt(MyTools.nigetPartByteArray(b, 0, 3));
//		byte type = b[4];
        startfreq = MyTools.byteToDouble(MyTools.getPartByteArray(b,
                5, 12));
        endfreq = MyTools.byteToDouble(MyTools.getPartByteArray(b,
                13, 20));
        count = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b,
                21, 24));
        // GlobalData.bujin=(GlobalData.startfreq-GlobalData.endfreq)/GlobalData.count;
        short[] save = new short[count];// 即频谱数据
        int n = 0;
        for (int i = 25; i < b.length; i = i + 2) {
            byte[] temp = new byte[]{b[i + 1], b[i]};
            save[n] = MyTools.twoBytesToShort(temp);
            n++;
        }
        pinpu = save;
    }

    public static void parseNewPinPu(byte[] b)// 解析频谱数据帧
    {
//		int length = MyTools.fourBytesToInt(MyTools
//				.nigetPartByteArray(b, 9, 12));
//		byte type = b[13];
        startfreq = MyTools.byteToDouble(MyTools.getPartByteArray(b,
                14, 21));
        endfreq = MyTools.byteToDouble(MyTools.getPartByteArray(b,
                22, 29));
        count = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b,
                30, 33));
        // GlobalData.bujin=(GlobalData.startfreq-GlobalData.endfreq)/GlobalData.count;
        short[] save = new short[count];// 即频谱数据
        int n = 0;
        for (int i = 34; i < b.length; i = i + 2) {
            byte[] temp = new byte[]{b[i], b[i + 1]};
            save[n] = MyTools.twoBytesToShort(temp);
            n++;
        }
        pinpu = save;
    }

    public static short parseDianping(byte[] b) {
        byte[] temp = new byte[]{b[23], b[22]};
        return (short) (Parse.twoBytesToShort(temp) / 10);
    }

    // 改写于2019.11.3
    public static void refreshUnManStation(byte[] b)// 无人站更新
    {
        try {
            int biaolength = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(
                    b, 4, 7)) - 1;// 帧头后的那个长度，减去表示功能号的1个字节。
            int temp = 0;
            while (biaolength - temp > 0) {
                int sflength = MyTools.fourBytesToInt(MyTools
                        .nigetPartByteArray(b, 9 + temp, 12 + temp));// 无人站帧的长度
                int stlength = sflength + 4;
                byte[] newinfo = MyTools.getPartByteArray(b, 9 + temp, 8
                        + stlength + temp);
                String id = new String(
                        MyTools.getPartByteArray(newinfo, 4, 79), "UTF-16LE");
                byte isavailable = newinfo[80];
                String state = new String(MyTools.getPartByteArray(newinfo, 81,
                        88), "UTF-16LE");
                String info = new String(MyTools.getPartByteArray(newinfo, 89,
                        stlength - 1), "UTF-16LE");
                unmanHashMap.get(id).state = state;
                unmanHashMap.get(id).info = info;
                unmanHashMap.get(id).isavailable = isavailable;
                if (state.startsWith("已经断")) {
                    unmanHashMap.get(id).isavailable = 6;
                } else if (state.startsWith("已经上")) {
                    unmanHashMap.get(id).isavailable = 4;
                }
                if (state.startsWith("已经掉")) {
                    unmanHashMap.get(id).isavailable = 1;
                }
                // 通知更新
                temp = temp + stlength;
            }
        } catch (Exception e) {
            Log.i("更新无人站", "异常");
        }
    }


    public static boolean isRefresh(){

        long time1 = SystemClock.uptimeMillis();
        if(time==0){
            time = time1;
            return true;
        }else {
            if(time1-time>10){
                time = time1;
                Log.d("refreshss","刷"+time);
                return true;
            }else {
                time = time1;
            }
        }
        return false;
    }

    public static int findkey(byte[] query,byte[] input)
    {
        int inputLength = input.length;
        int queryLength = query.length;

        int inputIndex = 0;
        int queryIndex = 0;

        if (queryLength > inputLength) {
            return -1;
        }

        while (inputIndex < (inputLength - queryLength)) {
            if (input[inputIndex] == query[queryIndex]) {
                inputIndex ++;
                queryIndex ++;
            } else {
                inputIndex ++;
                queryIndex = 0;
            }

            if (queryIndex == queryLength) { // 说明找到了
                break;
            }
        }

        return queryIndex == queryLength ? (inputIndex - queryLength) : -1;
    }

    static long time = 0;
    //static byte head[] = {(byte)0xee, (byte)0xee, (byte)0xee, (byte)0xee};
    public static void newParsePDScan(byte[] data) {
        int available = data.length;
        int index = 0;
        int fullframelength = 0;

        while (index < available - 3) {
//            index = findkey(head,data);
//            if (index < 0)
//                break;
//            available -= index;

            if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data, index,
                    index + 3)) == 0xEEEEEEEE)// 如果是帧头
            {
                int templength = MyTools.fourBytesToInt(MyTools
                        .nigetPartByteArray(data, index + 4, index + 7));// 具体帧的长度
                fullframelength = templength + 8;
                if (available - index >= fullframelength)// 说明当前帧是完整的
                {
                    switch (data[index + 8]) {
                        case 47:// 频段扫描数据
                            int temcount = MyTools.fourBytesToInt(MyTools
                                    .nigetPartByteArray(data, index + 14, index + 17));// 点数

                            int startindex = MyTools.fourBytesToInt(MyTools
                                    .nigetPartByteArray(data, index + 18, index + 21));
                            int endindex = MyTools.fourBytesToInt(MyTools
                                    .nigetPartByteArray(data, index + 22, index + 25));
                            short[] tempshort = null;

                            if (PinDuanBase.ScanType.equals("PSCAN")) {
                                if (data[index + 13] != 0 && isEnd == false)// 不是结束，并且上一个数组还未结束
                                {
                                    tempshort = pinduanQueue.getLast();
                                    System.out.println("哪里异常了？");
                                } else if (data[index + 13] != 0 && isEnd)// 不是结束，并且上一个数组结束了
                                {
                                    tempshort = new short[temcount];
                                    isEnd = false;
                                } else if (data[index + 13] == 0 && isEnd)// 是结束，并且上一个数组结束了
                                {
                                    tempshort = new short[temcount];
                                    isEnd = true;
                                } else if (data[index + 13] == 0
                                        && isEnd == false) {
                                    tempshort = pinduanQueue.getLast();

                                    isEnd = true;
                                }
                                if (tempshort == null) {
                                    tempshort = new short[temcount];
                                }

                                int part = startindex;

                                for (int i = index + 26; i < index
                                        + fullframelength - 1; i = i + 2) {
                                    tempshort[part] = MyTools.twoBytesToShort(
                                            new byte[]{data[i + 1], data[i]}) ;
                                    part++;
                                }

                                if (!pinduanQueue.contains(tempshort))
                                    pinduanQueue.add(tempshort);

                                if (data[index + 13] == 0)// 是结束
                                {
                                    if (pinduanMin == null) {
                                        pinduanMin = new short[temcount];
                                        for (int bc = 0; bc < temcount; bc++) {
                                            pinduanMin[bc] = 2550;
                                        }
                                    }

                                    if (pinduanMax == null) {
                                        pinduanMax = new short[temcount];
                                        for (int bc = 0; bc < temcount; bc++) {
                                            pinduanMax[bc] = -2550;
                                        }
                                    }

                                    if (pinduanAvg == null)
                                        pinduanAvg = new short[temcount];
                                    if (Avg == null)
                                        Avg = new float[temcount];

                                    for (int yy = 0; yy < temcount; yy++) {
                                        if (pinduanMax[yy] < tempshort[yy]) {
                                            pinduanMax[yy] = tempshort[yy];
                                        }

                                        if (pinduanMin[yy] > tempshort[yy]) {
                                            pinduanMin[yy] = tempshort[yy];
                                        }

                                        if (tempshort != null && isEnd) {
                                            Avg[yy] = (Avg[yy]
                                                    * haveScanCount + tempshort[yy] ) / (haveScanCount + 1);
                                            pinduanAvg[yy] =(short) Avg[yy];
                                        }
                                    }
                                    haveScanCount++;
                                    if(isRefresh()){
                                        if (temcount <= topValue) {
                                            PinDuanBase.handler
                                                    .sendEmptyMessage(PinDuanBase.SCANNINGDATA);
                                        } else {
                                            PinDuanBase.handler
                                                    .sendEmptyMessage(PinDuanBase.SCANNINGDATANO);
                                        }
                                    }
                                }
                            } else  // 非PSCAN模式
                            {
                                if (pinduanScan == null) {
                                    pinduanScan = new short[temcount];
                                }
                                if (pinduanMin == null) {
                                    pinduanMin = new short[temcount];
                                    for (int bc = 0; bc < temcount; bc++) {
                                        pinduanMin[bc] = 2550;
                                    }
                                }

                                if (pinduanMax == null) {
                                    pinduanMax = new short[temcount];
                                    for (int bc = 0; bc < temcount; bc++) {
                                        pinduanMax[bc] = -2550;
                                    }
                                }

                                if (pinduanAvg == null)
                                    pinduanAvg = new short[temcount];
                                if (Avg == null)
                                    Avg = new float[temcount];

                                int part = startindex;
                                for (int i = index + 26; i < index
                                        + fullframelength - 1; i = i + 2) {
                                    pinduanScan[part] = MyTools
                                            .twoBytesToShort(new byte[]{data[i + 1], data[i]});
                                    part++;
                                }

                                for (int yy = startindex; yy <= endindex; yy++) {
                                    if (pinduanMax[yy] < pinduanScan[yy]) {
                                        pinduanMax[yy] = pinduanScan[yy];
                                    }

                                    if (pinduanMin[yy] > pinduanScan[yy]) {
                                        pinduanMin[yy] = pinduanScan[yy];
                                    }

                                    Avg[yy] = (Avg[yy]
                                            * haveScanCount + pinduanScan[yy] ) / (haveScanCount + 1);
                                    pinduanAvg[yy] =(short) Avg[yy];
                                }
                                refreshLength = refreshLength
                                        + endindex - startindex + 1;
                                if (refreshLength >= temcount
                                        * fragment) {
                                    if(isRefresh()){
                                        if (temcount <= topValue) {

                                            PinDuanBase.handler
                                                    .sendEmptyMessage(PinDuanBase.SCANNINGDATAFSCAN);
                                        } else {
                                            PinDuanBase.handler
                                                    .sendEmptyMessage(PinDuanBase.SCANNINGDATAFSCANNO);
                                        }
                                    }
                                    fragment = fragment + 0.1f;
                                }

                                if (data[index + 13] == 0)// 是结束
                                {
                                    haveScanCount++;
                                    fragment = 0.1f;
                                }
                            }
                            break;
                        case 33:
                            break;
                        case 34:// 因子
                            int l = MyTools.fourBytesToInt(MyTools
                                    .nigetPartByteArray(data, index + 30, index + 33));
                            PDyinzi = new short[l];
                            int g = 0;
                            try {
                                for (int v = index + 34; v < index
                                        + fullframelength - 1; v = v + 2) {
                                    PDyinzi[g] = (short) (MyTools
                                            .twoBytesToShort(new byte[]{data[v + 1],
                                                    data[v]}) / 10);
                                    g++;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    index = index + fullframelength;
                } else {// 当前帧是不完整的
                    pinduanbuffer = new byte[fullframelength];
                    // Log.i("当前帧不完整","帧长"+fullframelength+" ");
                    System.arraycopy(data, index, pinduanbuffer, 0,
                            available - index);
                    // Log.i("不完整帧当前索引",GlobalData.bufferIndex+"  ");
                    pinduanbufferIndex = available - index;
                    // Log.i("不完整帧当前最新索引",GlobalData.bufferIndex+"  ");
                    index = available - 1;
                }
            } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data,
                    index, index + 3)) != 0xEEEEEEEE
                    && pinduanbuffer != null) {// 如果不是帧头并且GlobalData.pinduanbuffer不为空，说明是前一帧的后续

                // Log.i("后续部分到了","出发");
                int templength = Math.min(pinduanbuffer.length - pinduanbufferIndex,
                                          data.length);
                System.arraycopy(data, 0, pinduanbuffer,
                        pinduanbufferIndex, templength);
                pinduanbufferIndex = pinduanbufferIndex + templength;
                if (pinduanbufferIndex == pinduanbuffer.length) {
                    Parse.newParsePDScan(pinduanbuffer);
                    pinduanbuffer = null;
                    pinduanbufferIndex = 0;
                }
                index = index + templength;
            }
            else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data,
                    index, index + 3)) != 0xEEEEEEEE
                    && pinduanbuffer == null) {
                // 如果不是帧头并且GlobalData.pinduanbuffer为空，说明有漏帧,需要抛弃部分数据

                // Log.i("有漏帧", "发生");
                int m;
                m = 0;
                for (m = index; m < available ; m += 3) {
                    if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data, m,
                            m + 3)) == 0xEEEEEEEE) {
                        break;
                    }
                }
                if (m < available-3) {
                    if (data.length - m > 0) {
                        try {
                            byte[] tmpbytes = new byte[data.length - m];
                            System.arraycopy(data, m, tmpbytes, 0, data.length - m);
                            index = available - 1;
                            //Log.i("有漏帧", "递归");
                            Parse.newParsePDScan(tmpbytes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                // 如果不是帧头并且GlobalData.pinduanbuffer为空，说明有漏帧,需要抛弃部分数据

                // Log.i("有漏帧", "发生");

//                byte[] data2 = new byte[available-index];
//                System.arraycopy(data, index, data2, 0,
//                        available - index);
//                int index2= findkey(head,data2);
//                if (index2 < 0)
//                    break;
//                else
//                    index += index2;
//
//                available -= index;
//
//                if (index < available) {
//                    if (available > 0) {
//                        try {
//                            byte[] tmpbytes = new byte[available];
//                            System.arraycopy(data, index, tmpbytes, 0, available);
//                            //index = available - 1;
//                            //Log.i("有漏帧", "递归");
//                            Parse.newParsePDScan(tmpbytes);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
            }
        }
    }

    public static void newParseMScan(byte[] data) {
        int available = data.length;
        int index = 0;
        int fullframelength = 0;
        while (index < available - 3) {
            if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data, index,
                    index + 3)) == 0xEEEEEEEE)// 如果是帧头
            {
                int templength = MyTools.fourBytesToInt(MyTools
                        .nigetPartByteArray(data, index + 4, index + 7));// 具体帧的长度
                fullframelength = templength + 8;
                if (available - index >= fullframelength)// 说明当前帧是完整的
                {
                    switch (data[index + 8]) {
                        case 49:// 离散扫描数据
                            int temcount = MyTools.fourBytesToInt(MyTools
                                    .nigetPartByteArray(data, index + 14, index + 17));// 点数

                            int startindex = MyTools.fourBytesToInt(MyTools
                                    .nigetPartByteArray(data, index + 18, index + 21));
                            int endindex = MyTools.fourBytesToInt(MyTools
                                    .nigetPartByteArray(data, index + 22, index + 25));
                            float[] tempshort = new float[temcount];

                            int part = startindex;

                            for (int i = index + 26; i < index
                                    + fullframelength - 1; i = i + 2) {
                                tempshort[part] =  MyTools
                                        .twoBytesToShort(new byte[]{data[i + 1],
                                                data[i]}) / 10f;
                                part++;
                            }

                            if (mscandata == null || mscan_count != temcount) {
                                mscandata = new float[temcount];
                                mscan_count = temcount;
                            }

                            for (int i = 0; i<temcount; i++)
                                mscandata[i] = tempshort[i];

                            MscanBase.handler.sendEmptyMessage(MScanActivity.MSCANNINGDATA);

                            break;

                        case 33:
                            break;
                        case 34:// 因子
                            int l = MyTools.fourBytesToInt(MyTools
                                    .nigetPartByteArray(data, index + 30, index + 33));
                            PDyinzi = new short[l];
                            int g = 0;
                            try {
                                for (int v = index + 34; v < index
                                        + fullframelength - 1; v = v + 2) {
                                    PDyinzi[g] = (short) (MyTools
                                            .twoBytesToShort(new byte[]{data[v + 1],
                                                    data[v]}) / 10);
                                    g++;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    index = index + fullframelength;
                } else {// 当前帧是不完整的

                    pinduanbuffer = new byte[fullframelength];
                    // Log.i("当前帧不完整","帧长"+fullframelength+" ");
                    System.arraycopy(data, index, pinduanbuffer, 0,
                            available - index);
                    // Log.i("不完整帧当前索引",GlobalData.bufferIndex+"  ");
                    pinduanbufferIndex = available - index;
                    // Log.i("不完整帧当前最新索引",GlobalData.bufferIndex+"  ");
                    index = available - 1;
                }
            } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data,
                    index, index + 3)) != 0xEEEEEEEE
                    && pinduanbuffer != null) {// 如果不是帧头并且GlobalData.pinduanbuffer不为空，说明是前一帧的后续

                // Log.i("后续部分到了","出发");
                int templength = Math.min(pinduanbuffer.length
                        - pinduanbufferIndex, data.length);
                System.arraycopy(data, 0, pinduanbuffer,
                        pinduanbufferIndex, templength);
                pinduanbufferIndex = pinduanbufferIndex
                        + templength;
                if (pinduanbufferIndex == pinduanbuffer.length) {
                    Parse.newParsePDScan(pinduanbuffer);
                    pinduanbuffer = null;
                    pinduanbufferIndex = 0;
                }
                index = index + templength;
            } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data,
                    index, index + 3)) != 0xEEEEEEEE
                    && pinduanbuffer == null) {// 如果不是帧头并且GlobalData.pinduanbuffer为空，说明有漏帧,需要抛弃部分数据

                // Log.i("有漏帧", "发生");
                int m = 0;
                for (m = 0; m < available - 3; m++) {
                    if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(data, m,
                            m + 3)) == 0xEEEEEEEE) {
                        break;
                    }
                }
                // m=m-1;
                byte[] regment = new byte[data.length - m];
                System.arraycopy(data, m, regment, 0, data.length - m);
                index = available - 1;
                Parse.newParseMScan(regment);
            }
        }
    }

    public static short[] parsePDScan(byte[] b) {
        int alllength = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b, 4,
                7));
        int availablelength = 0;
        byte isend = 0;
        int count = 0;
        int startindex = 0, endindex = 0;
        short[] data = null;
        if (alllength > 1) {
            availablelength = MyTools.fourBytesToInt(MyTools
                    .nigetPartByteArray(b, 9, 12));
            isend = b[13];
            count = MyTools.fourBytesToInt(MyTools
                    .nigetPartByteArray(b, 14, 17));
            startindex = MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b,
                    18, 21));
            if (availablelength > 13) {
                data = new short[(availablelength - 13) / 2];
                int m = 0;
                for (int i = 26; i < b.length; i = i + 2) {
                    data[m] = MyTools.twoBytesToShort(MyTools
                            .nigetPartByteArray(b, i, i + 1));
                }
            }
        }
        return data;
    }

    public static void refreshDevice(byte[] b) {
        String stationId = null;
        try {
            stationId = new String(MyTools.getPartByteArray(b, 9, 84),
                    "UTF-16LE").trim();
        } catch (Exception e) {
            System.out.println("设备更新解析stationId时出现异常");
        }
        String devicename = null;
        try {
            devicename = new String(MyTools.getPartByteArray(b, 85, 120),
                    "UTF-16LE").trim();
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
        byte state = b[121];
        Station s = stationHashMap.get(stationId);

        for (MyDevice m : s.devicelist) {
            if (m.name.equals(devicename)) {
                MyDevice md = m;
                md.isOccupied = state;
                break;
            }
        }
        try {
            if (StationListActivity.handler != null)
                StationListActivity.handler
                        .sendEmptyMessage(StationListActivity.REFRESHSTATE);
            if (StationShowFragment.handler != null){
                StationShowFragment.handler
                        .sendEmptyMessage(0);
            }
        } catch (Exception e) {
            System.out.println("台站设备刷新消息通知失败！");
        }
        if (MapActivity.handler != null) {
            MapActivity.handler.sendEmptyMessage(MapActivity.STATIONREFRESH);
        }
    }

    public static void stationDown(byte[] b) {
        try {
            String id = new String(MyTools.getPartByteArray(b, 9, 84),
                    "UTF-16LE").trim();
            stationHashMap.remove(id);
        } catch (Exception e) {
            System.out.println("解析监测站下线时发生错误");
        }
    }

    public static void parseVoice(byte[] b) {
        int length = MyTools
                .fourBytesToInt(MyTools.nigetPartByteArray(b, 0, 3));
        byte type = b[4];
        byte format = b[5];
        short[] data = new short[(length - 4) / 2];
        int m = 0;
        if (length > 6) {
            for (int i = 6; i < b.length; i = i + 2) {
                data[m] = MyTools.twoBytesToShort(MyTools.nigetPartByteArray(b, i, i + 1));
            }
        }
    }

    public static void parseShixiangdu(byte[] b) {
        byte function = b[8];
        if (function == 46) {
            switch (b[13]) {
                case 31:// 电平数据
                    DDFdianping = Parse.parseDianping(b);
                    // SinglefrequencyDFActivity.handler.sendEmptyMessage(0x8);
                    break;
                case 37:// 示向度测量数据
                    // Parse.parseShixiangdu(MyTools.getPartByteArray(b, 9,
                    // b.length-1));

                    north = (short) (MyTools.twoBytesToShort(MyTools
                            .nigetPartByteArray(b, 14, 15)) / (short) 10);
                    xiangdui = (short) (MyTools.twoBytesToShort(MyTools
                            .nigetPartByteArray(b, 16, 17)) / (short) 10);
                    qua = (short) (MyTools.twoBytesToShort(MyTools
                            .nigetPartByteArray(b, 18, 19)) / (short) 10);
                    double centerFreq = MyTools.byteToDouble(MyTools
                            .getPartByteArray(b, 20, 27));

                    // SinglefrequencyDFActivity.handler.sendEmptyMessage(0x8);
                    break;
                case 36:// 频谱数据
                    // byte[] pinpu=new byte[b.length-9];
                    // System.arraycopy(b, 0, pinpu, 0, pinpu.length);
                    // Parse.parsePinPu(pinpu);
                    Parse.parseNewPinPu(b);
                    // SinglefrequencyDFActivity.handler.sendEmptyMessage(0x9);
                    break;
            }
        }
    }

    private static short twoBytesToShort(byte[] b) {
        return (short) (((b[0] & 0xff) << 8) | (b[1] & 0xff));
    }

    private static float fourBytesToFloat(byte[] b) {
        // 4 bytes
        int accum = 0;
        for (int shiftBy = 0; shiftBy < 4; shiftBy++) {
            accum |= (b[shiftBy] & 0xff) << shiftBy * 8;
        }
        return Float.intBitsToFloat(accum);
    }

    private static byte[] float2byte(float f) {
        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 翻转数组
        int len = b.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }
        return dest;
    }

    public static Handler handlerm = null;

    public static void setHandler(Handler handler) {
        handlerm = handler;
    }

    public static void parseDDF(byte[] src)// 最新的单频测向解析
    {
        try {
            int available = src.length;
            int index = 0;
            int fullframelength = 0;
            while (index < available - 3) {
                if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(src, index,
                        index + 3)) == 0xEEEEEEEE)// 如果是帧头
                {
                    int templength = MyTools.fourBytesToInt(MyTools
                            .nigetPartByteArray(src, index + 4, index + 7));// 具体帧的长度
                    fullframelength = templength + 8;
                    if (available - index >= fullframelength)// 说明当前帧是完整的
                    {
                        switch (src[index + 8]) {

                            case 46:
                                switch (src[index + 13]) {
                                    case 31:// 电平
                                        byte[] temp = new byte[]{src[23 + index], src[22 + index]};
                                        DDFdianping = (short) (Parse
                                                .twoBytesToShort(temp) / 10);
                                        (handlerm == null ? SinglefrequencyDFActivity.handle : handlerm)
                                                .sendEmptyMessage(SinglefrequencyDFActivity.PARTFRESH);
                                        break;
                                    case 37:// 示向度
                                        north = (short) (MyTools.twoBytesToShort(MyTools
                                                .nigetPartByteArray(src, index + 14, index + 15)) / (short) 10);
                                        xiangdui = (short) (MyTools
                                                .twoBytesToShort(MyTools.nigetPartByteArray(src,
                                                        index + 16, index + 17)) / (short) 10);
                                        qua = (short) (MyTools.twoBytesToShort(MyTools
                                                .nigetPartByteArray(src, index + 18, index + 19)) / (short) 10);
                                        double centerFreq = MyTools.byteToDouble(MyTools
                                                .getPartByteArray(src, index + 20, index + 27));
                                        if (SinglefrequencyDFActivity.handle != null)
                                            (handlerm == null ? SinglefrequencyDFActivity.handle : handlerm)
                                                    .sendEmptyMessage(SinglefrequencyDFActivity.PARTFRESH);
                                        if (MapActivity.handler != null) {
                                            MapActivity.handler
                                                    .sendEmptyMessage(MapActivity.SHISHIHUAXIAN);
                                        }
                                        break;
                                    case 36:// 频谱数据
                                        startfreq = MyTools.byteToDouble(MyTools
                                                .getPartByteArray(src, index + 14, index + 21));
                                        endfreq = MyTools.byteToDouble(MyTools
                                                .getPartByteArray(src, index + 22, index + 29));
                                        count = MyTools.fourBytesToInt(MyTools
                                                .nigetPartByteArray(src, index + 30, index + 33));
                                        // GlobalData.bujin=(GlobalData.startfreq-GlobalData.endfreq)/GlobalData.count;
                                        short[] save = new short[count];// 即频谱数据
                                        int n = 0;
                                        for (int i = index + 34; i < index + fullframelength - 1
                                                && n < count; i = i + 2) {
                                            byte[] temps = new byte[]{src[i + 1], src[i]};
                                            save[n] = (short) (MyTools.twoBytesToShort(temps) / 10);//byte[]转为short[]数据,长度减半了
                                            n++;
                                        }
                                        pinpu = save;
                                        (handlerm == null ? SinglefrequencyDFActivity.handle : handlerm)
                                                .sendEmptyMessage(SinglefrequencyDFActivity.NETREFRESH);
                                        break;
                                }
                                break;
                            case 35:// 声音帧
                            case 39:
                                newaudiotype = src[index + 8];
//                                if (newaudiotype != oldaudiotype)
//                                    break;
                                boolean gsm610 = false;
//                                if (newaudiotype == 39)
//                                    gsm610 = true;

                                int framelength = fullframelength - 21 ;
                                Log.i("当前帧完整", "帧长" + fullframelength + " ");
                                if (newaudiotype != oldaudiotype || isFirstAudio) {
                                    int caiyangpinlv = MyTools
                                            .fourBytesToInt(MyTools
                                                    .nigetPartByteArray(src, index + 17, index + 20));
                                    short channelcount = MyTools
                                            .twoBytesToShort(new byte[]{
                                                    src[index + 16], src[index + 15]});
                                    byte geshi = src[index + 14];

                                    if (!gsm610) {
                                        AnalysisBase.createAudioPlay(
                                                caiyangpinlv, geshi, channelcount);
                                    } else {
                                        gsmdecoder = new GSMDecoder(true);
                                        AnalysisBase.createAudioPlay(
                                                8000, geshi, channelcount);
                                    }
//                                    try{
//                                        Thread.sleep(100);
//                                    }catch (Exception e){
//                                        e.printStackTrace();
//                                    }
                                    isFirstAudio = false;
                                }
                                byte[] finalSrc = src;
                                int finalIndex = index;
                                boolean gsm = gsm610;

                                AnalysisBase.executor.execute(() -> {
                                    try {
                                        AudioScadule(finalSrc, finalIndex, framelength, false);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                                oldaudiotype = newaudiotype;
                                break;
                            case 34:// 因子
                                int l = MyTools.fourBytesToInt(MyTools
                                        .nigetPartByteArray(src, index + 30,
                                                index + 33));
                                yinzi = new short[l];
                                int g = 0;
                                for (int v = index + 34; v < index
                                        + fullframelength - 1; v = v + 2) {
                                    yinzi[g] = (short) (MyTools
                                            .twoBytesToShort(new byte[]{src[v + 1],
                                                    src[v]}) / 10);
                                    g++;
                                }
                                break;
                            case 33:// IQ
                                int c = MyTools.fourBytesToInt(MyTools
                                        .nigetPartByteArray(src, index + 23,
                                                index + 26));
                                int ui = 0;
                                int uq = 0;
                                switch (src[index + 22]) {
                                    case 0:
                                        for (int r = index + 27; r < index + c * 2 + 26; r = r + 2) {
                                            siBuffer[ui] = MyTools
                                                    .twoBytesToShort(new byte[]{
                                                            src[r + 1], src[r]});
                                            ui++;
                                        }
                                        for (int w = index + 27 + c * 2; w < index + 4
                                                * c + 26; w = w + 2) {
                                            sqBuffer[uq] = MyTools
                                                    .twoBytesToShort(new byte[]{
                                                            src[w + 1], src[w]});
                                            uq++;
                                        }
                                        break;
                                    case 1:
                                        for (int r = index + 27; r < index + 4 * c + 23; r = r + 4) {
                                            iBuffer[ui] = MyTools
                                                    .twoBytesToShort(MyTools
                                                            .nigetPartByteArray(src, r,
                                                                    r + 3));
                                            ui++;
                                        }
                                        for (int w = index + 27 + c * 4; w < index + 8
                                                * c + 23; w = w + 4) {
                                            qBuffer[uq] = MyTools
                                                    .twoBytesToShort(MyTools
                                                            .nigetPartByteArray(src, w,
                                                                    w + 3));
                                            uq++;
                                        }
                                        break;
                                }
                                break;
                        }
                    } else {// 当前帧是不完整的
                        buffer = new byte[fullframelength];
                        Log.i("当前帧不完整", "帧长" + fullframelength + " ");
                        System.arraycopy(src, index, buffer, 0,
                                available - index);
                        Log.i("不完整帧当前索引", bufferIndex + "  ");
                        bufferIndex = available - index;
                        Log.i("不完整帧当前最新索引", bufferIndex + "  ");
                    }
                    index = index + fullframelength;
                } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(src,
                        index, index + 3)) != 0xEEEEEEEE)
                // &&
                // GlobalData.buffer!=null)//如果不是帧头并且GlobalData.buffer不为空，说明是前一帧的后续
                {
                    Log.i("后续部分到了", "出发");
                    int templength = Math.min(buffer.length
                            - bufferIndex, src.length);
                    System.arraycopy(src, 0, buffer,
                            bufferIndex, templength);
                    bufferIndex = bufferIndex
                            + templength;
                    if (bufferIndex == buffer.length) {
                        Parse.newParseSpectrumsAnalysis(buffer);
                        buffer = null;
                        bufferIndex = 0;
                    }
                    index = index + templength;
                } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(src,
                        index, index + 3)) != 0xEEEEEEEE
                        && buffer == null)// 如果不是帧头并且GlobalData.buffer为空，说明有漏帧,需要抛弃部分数据
                {
                    Log.i("有漏帧", "发生");
                    int m = 0;
                    for (m = 0; m < available - 3; m++) {
                        if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(
                                src, m, m + 3)) == 0xEEEEEEEE) {
                            break;
                        }
                    }
                    m = m - 1;
                    byte[] regment = new byte[src.length - m];
                    System.arraycopy(src, m, regment, 0, src.length - m);
                    index = available - 1;
                    Parse.newParseSpectrumsAnalysis(regment);
                }
            }

            System.gc();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void parseDDFFull(byte[] b) {
        int all = b.length;
        if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b, 0, 3)) == 0xEEEEEEEE)// 如果是帧头
        {
            framelength = MyTools.fourBytesToInt(MyTools
                    .nigetPartByteArray(b, 4, 7));
            if (all - 8 == framelength)// 正好是一个完整的帧
            {
                parseDDF(b);
            } else if (all - 8 < framelength)// 这个帧不够，还有后续部分没有到来
            {
                withFrameHeadBuffer = new byte[framelength + 8];
                System.arraycopy(b, 0, withFrameHeadBuffer, 0, all);
            }
        } else {// 如果不是帧头，说明是前一帧的后续部分

            if (withFrameHeadBuffer != null) {

            }
        }
    }

    // 重写于2014.10.23
    public static void newParseUnManServer(byte[] b) throws Exception {
        int available = b.length;
        int index = 0;
        int fullframelength = 0;
        while (index < available - 3) {
            if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b, index,
                    index + 3)) == 0xEEEEEEEE)// 如果是帧头
            {
                int templength = MyTools.fourBytesToInt(MyTools
                        .nigetPartByteArray(b, index + 4, index + 7));// 具体帧的长度，即帧头后的长度
                fullframelength = templength + 8;
                if (available - index >= fullframelength)// 说明当前帧是完整的
                {
                    UnManServer unmanserver = new UnManServer();
                    unmanserver.name = new String(MyTools.getPartByteArray(b,
                            index + 13, index + 48), "UTF-16LE");
                    unmanserver.id = new String(MyTools.getPartByteArray(b,
                            index + 49, index + 124), "UTF-16LE");
                    unmanserver.parentId = new String(MyTools.getPartByteArray(
                            b, index + 125, index + 200), "UTF-16LE");
                    short infolength = MyTools.twoBytesToShort(new byte[]{
                            b[index + 202], b[index + 201]});
                    short infolengths = MyTools.twoBytesToShort(new byte[]{
                            b[index + 201], b[index + 202]});
                    if (infolength != 0) {
                        unmanserver.sonstationIdArray = new String(
                                MyTools.getPartByteArray(b, index + 203, index
                                        + 202 + infolength)).split("\\|");// 特别注意：第一个值可能是空的
                    }

                    unmanServerHashMap.put(unmanserver.id, unmanserver);
                    int stationslength = fullframelength - (203 + infolength);// 该服务器上所有无人站数据的总长度

                    if (stationslength != 0)// 说明有无人站挂载
                    {
                        int havelength = 0;// 已经解析了的字节数。此处该变量每次涨值都是一个无人站的字节数。
                        while ((index + 203 + infolength + havelength) < (index + fullframelength)) {
                            int currentstationlength = MyTools
                                    .fourBytesToInt(MyTools.nigetPartByteArray(
                                            b, index + 203 + infolength
                                                    + havelength, index + 206
                                                    + infolength + havelength));
                            UnManStation station = new UnManStation();
                            station.id = new String(MyTools.getPartByteArray(b,
                                    index + 207 + infolength + havelength,
                                    index + 282 + infolength + havelength),
                                    "UTF-16LE");
                            station.name = new String(MyTools.getPartByteArray(
                                    b, index + 283 + infolength + havelength,
                                    index + 318 + infolength + havelength),
                                    "UTF-16LE");
                            String lantemp = df
                                    .format(MyTools.byteToDouble(MyTools
                                            .getPartByteArray(b, index + 319
                                                            + infolength + havelength,
                                                    index + 326 + infolength
                                                            + havelength)));// 纬度
                            station.lan = Double.valueOf(lantemp);
                            String lontemp = df
                                    .format(MyTools.byteToDouble(MyTools
                                            .getPartByteArray(b, index + 327
                                                            + infolength + havelength,
                                                    index + 334 + infolength
                                                            + havelength)));// 纬度
                            station.lon = Double.valueOf(lontemp);
                            station.isavailable = b[index + 335 + infolength
                                    + havelength];
                            station.iskongtiao = b[index + 336 + infolength
                                    + havelength];
                            station.switcharray = new String(
                                    MyTools.getPartByteArray(b, index + 337
                                            + infolength + havelength, index
                                            + 336 + infolength + havelength
                                            + currentstationlength - 130),
                                    "UTF-16LE").split("\\|");
                            station.server = unmanserver.name;
                            unmanHashMap.put(station.id, station);
                            havelength = havelength + currentstationlength + 4;
                        }
                    }
                    index = index + fullframelength;
                    // UnmanedStationsActivity.handler.sendEmptyMessage(UnmanedStationsActivity.UNMANDATA);
                } else// 当前帧是不完整的
                {
                    unmanbuffer = new byte[fullframelength];
                    System.arraycopy(b, index, unmanbuffer, 0,
                            available - index);
                    unmanbufferindex = available - index;
                    index = available - 1;
                }
            } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b,
                    index, index + 3)) != 0xEEEEEEEE
                    && unmanbuffer != null)// 如果不是帧头并且GlobalData.pinduanbuffer不为空，说明是前一帧的后续
            {
                int templength = Math.min(unmanbuffer.length
                        - unmanbufferindex, b.length);
                System.arraycopy(b, 0, unmanbuffer,
                        unmanbufferindex, templength);
                unmanbufferindex = unmanbufferindex
                        + templength;
                if (unmanbufferindex == unmanbuffer.length) {
                    Parse.newParseUnManServer(unmanbuffer);
                    unmanbuffer = null;
                    unmanbufferindex = 0;
                }
                index = index + templength;
            } else if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b,
                    index, index + 3)) != 0xEEEEEEEE
                    && unmanbuffer == null)// 如果不是帧头并且GlobalData.unmanbuffer为空，说明有漏帧,需要抛弃部分数据
            {
                int m = 0;
                for (m = 0; m < available - 3; m++) {
                    if (MyTools.fourBytesToInt(MyTools.nigetPartByteArray(b, m, m + 3)) == 0xEEEEEEEE) {
                        break;
                    }
                }
                m = m - 1;
                byte[] regment = new byte[b.length - m];
                System.arraycopy(b, m, regment, 0, b.length - m);
                Parse.newParseUnManServer(b);
                index = m;
            }
        }
        if (StationShowFragment.handler != null)
            StationShowFragment.handler.sendEmptyMessage(0x8);
    }
}

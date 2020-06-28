package com.huari.Presenter.entity;

import android.os.Environment;

public class Constant {

    public static int PORT = 19191;
    public static int DeviceStateRequestDelay = 30000;

    public static String IP = "192.168.1.109";
    public static String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    public static String TMP_FILE = "tmpfile";
    public static String TMP_FILE_HEADER = "tmpfile";
    public static String SINGLE_MEASURE_FLAG = "DF";
    public static String Connected = "Connected";
    public static String Disconnected = "Disconnected";
    public static String Spectrum = "Spectrum";
    public static String CallbackReeslt_Success = "success";

    /*
    事件命名原则为可唯一指定且能与其他时间区分开的任意字符序列

    事件组的命名原则为需要单独一路数据的任务应该单独开辟一个事件组序列号（需要单独开一路数据的可能原因有：
     1.操作可能对传入的数据做出修改，导致此节点后续链路上的其他的节点数据不可用,但是也可以同过在链路上的修改执行前对数据进行深拷贝后传入后方
     2.为耗时操作，应该并行执行的情况，但是也同样可能通过每个Transaction都会持有的线程池来将耗时操作放到线程池中执行，然后将数据直接丢给后续节点）
    */

    //结束的回调任务
    public static String EndCallback = "StopTask";

    //命令请求回调
    public static String Callback = "Callback";

    //连接状态监听
    public static int ConnectionStateGroup = -1;

    //设备状态监听任务
    public static String DeviceState = "DeviceState";

    public static int DeviceStateGroup = 0;

    //单频测量
    public static String SingleMeasureStart = "SingleMeasureStart";

    public static String IQSave = "IQSave";

    public static String Audio = "Audio";

    public static String ITU = "ITU";

    public static String MR = "ModulationRecognition";

    public static String AudioSave = "AudioSave";

    public static String SingleMeasureSave = "SingleMeasureSave";

    public static String AudioDataPretreatmentTransaction = "AudioDataPretreatmentTransaction";

    public static int CallBackID = 1;//命令状态回复事件组编号

    public static int SingleMeasureGroup = 2;//单频测量UI主数据解析事件组编号

    public static int AudioGroup = 3;//音频相关事件组

    public static int ITUGroup = 4;//单频ITU算法事件组（为了避免因为算法对数据的更改而导致的问题，所以每个算法要拥有一个事件组号）

    public static int ModulationRecognitionGroup = 5;//调制解调算法事件组

    public static int SingleMeasureDataSaveGroup = 6;//单频常规数据的持久化事件组

    public static int AudioDataSaveGroup = 7;//音频数据存储

    public static int IQdataSaveGroup = 8;//IQ数据存储


    //PScan
    public static String CallbackPScan = "CallbackPScanCallback"; //PScan回调事件

    public static String PScanStart = "PScanStart";//PScan开始事件

    public static String FreqyencyHopping = "FreqyencyHopping";//跳频识别算法

    public static String SignalSort = "SignalSort";//信号分选算法

    public static String SpectrumParse = "SpectrumParse";//频谱数据分包等待

    public static int PScanStartGroup = 12;//PScan开始事件组

    public static int PScanArithmetic = 13;//PScan算法事件组


    //Mscan
    public static String CallbackMscan = "CallbackMscan"; //PScan回调事件

    public static String MScanStart = "MScanStart";//PScan开始事件

    public static int MScanStartGroup = 14;//Mscan开始事件组


    //MultiSignal
    public static String MultiSignalStart = "MultiSignalStart";//PScan开始事件

    public static int MultiSignalGroup = 15;//MultiSignal开始事件组


    //HopDectector
    public static String HopDectectorStart = "HopDectectorStart";//跳频事件
    public static int HopDectectorGroup = 16;//HopDectector事件组
}

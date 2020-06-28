package com.huari.Presenter.BusinessCore;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.cdhuari.entity.AudioData;
import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.Impl.FileWorkImpl;
import com.huari.Presenter.Impl.HopDectectorArithmetic;
import com.huari.Presenter.Impl.ITUParamterArithmetic;
import com.huari.Presenter.Impl.ModulationrecognitionArithmetic;
import com.huari.Presenter.Impl.NetTaskScheduleImpl;
import com.huari.Presenter.Impl.NetWorkImpl;
import com.huari.Presenter.Impl.SignalSortArithmetic;
import com.huari.Presenter.Interface.DataWork;
import com.huari.Presenter.Transactions.CommonTransactions.Factory;
import com.huari.Presenter.Transactions.ConnectionState.ConnectionStateTransaction;
import com.huari.Presenter.Transactions.DeviceState.DeviceStateTransaction;
import com.huari.Presenter.Transactions.FreqScan.FrequencyHoppingTransaction;
import com.huari.Presenter.Transactions.FreqScan.PScanTransaction;
import com.huari.Presenter.Transactions.FreqScan.SignalSortTransaction;
import com.huari.Presenter.Transactions.Mscan.MscanTransaction;
import com.huari.Presenter.Transactions.MutilSignal.MultiTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.AudioDataPersistenceFileTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.AudioPlayerTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.IQSaveTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.ITUTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.ModulationRecognitionTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.SingleMeasureSaveTransaction;
import com.huari.Presenter.Transactions.Singlemeasure.SingleMeasureTransaction;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.Request;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BusinessCoreNet {
    private volatile static BusinessCoreNet businessCoreNet;
    WeakReference<IBaseView> onDeviceStateListener;
    WeakReference<IBaseView> onConnectionListener;
    Map<String, Transaction> map;
    TaskSchedule NetTaskSchedule;
    DataWork dataWork;
    DataWork fileWork;
    boolean ConnectionState;

    private BusinessCoreNet() {
        map = new HashMap<>();
        dataWork = new NetWorkImpl();
        fileWork = new FileWorkImpl();
        NetTaskSchedule = NetTaskScheduleImpl.getSingleNetWork(new NetTaskScheduleImpl.Builder(dataWork));
        AddConnectionStateListener();
        NetTaskSchedule.startNetWork(dataWork);//不要直接调用network的start，因为在TaskSchedule的startNetWork方法封装了对状态处理的任务的添加，这是设备自检状态处理功能正常执行的关键。
        NetTaskSchedule.startFileWork(fileWork);
    }

    public static BusinessCoreNet getInstanceNet() {
        if (businessCoreNet == null) {
            synchronized (BusinessCoreNet.class) {
                if (businessCoreNet == null) {
                    businessCoreNet = new BusinessCoreNet();
                }
            }
        }
        return businessCoreNet;
    }

    public Map<String, Transaction> getMap() {
        return map;
    }

    /**
     * 定时请求设备状态
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Request request = new Request();
            request.type = DataTypeEnum.DeviceInfo.toString();
            request.list = new ArrayList<>();
            while (ConnectionState) {
                NetTaskSchedule.sendCommandNet(request);
                try {
                    Thread.sleep(Constant.DeviceStateRequestDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 添加设备状态任务
     */
    public void AddDeviceStateListener() {
        if (!map.containsKey(DataTypeEnum.DeviceInfo.toString())) {
            DeviceStateTransaction deviceStateTransaction = (DeviceStateTransaction) Factory.getInstance().createTransaction(DataTypeEnum.DeviceInfo.toString(), NetTaskSchedule, null);
            NetTaskSchedule.addTransaction(deviceStateTransaction);
            deviceStateTransaction.setOnDeviceStateListener(null);
            map.put(DataTypeEnum.DeviceInfo.toString(), deviceStateTransaction);
        }
    }

    /**
     * 设置设备状态的监听回调
     */
    public void setOnDeviceStateListener(IBaseView baseView) {
        this.onDeviceStateListener = new WeakReference<>(baseView);
        ((DeviceStateTransaction) map.get(DataTypeEnum.DeviceInfo.toString())).setOnDeviceStateListener(baseView);
    }

    /**
     * 添加连接状态监听
     */
    public void AddConnectionStateListener() {
        if (!map.containsKey(DataTypeEnum.ConnectionState.toString())) {
            ConnectionStateTransaction deviceStateTransaction = (ConnectionStateTransaction) Factory.getInstance().createTransaction(DataTypeEnum.ConnectionState.toString(), NetTaskSchedule, (IBaseView) onConnectionListener);
            deviceStateTransaction.setConnectionStateListener(string -> {
                if (string.equals(Constant.Connected)) {
                    AddDeviceStateListener();
                    ConnectionState = true;
                    NetTaskSchedule.IOexecutorService.execute(runnable);
                } else {
                    DisConnection();
                }
            });
            NetTaskSchedule.addTransaction(deviceStateTransaction);
            map.put(DataTypeEnum.ConnectionState.toString(), deviceStateTransaction);
        }
    }

    public void DisConnection() {
        ConnectionState = false;
        NetTaskSchedule.clearTaskExeceptIdGroup(Arrays.asList(-1));
        map.clear();
    }

    public void setConnectionStateListener(IBaseView iBaseView) {
        this.onConnectionListener = new WeakReference<>(iBaseView);
    }

    /**
     * 单频开始
     *
     * @param request
     */
    public void SingleMeasureStart(final Request request, IBaseView baseView) {
        SingleMeasureStartTaskHandle singleMeasureStartTaskHandle = new SingleMeasureStartTaskHandle(new SingleMeasureStartTaskHandle.SingalMeasureParameter(request, baseView, Constant.SingleMeasureStart, map, NetTaskSchedule));
        singleMeasureStartTaskHandle.start();
    }

    public void SingleMeasureStart(final String request, IBaseView baseView) {
        fileWork.sendMessage(request);
        Factory factory = Factory.getInstance();
        SingleMeasureTransaction singleMeasureTransaction = (SingleMeasureTransaction) factory.createTransaction(Constant.SingleMeasureStart, NetTaskSchedule, baseView);
        NetTaskSchedule.addTransaction(singleMeasureTransaction);
        map.put(Constant.SingleMeasureStart, singleMeasureTransaction);
    }

    /**
     * 单频结束
     *
     * @param request
     */
    public void SingleMeasureEnd(final Request request, IBaseView baseView) {
        SingleMeasureEndTaskHandle singleMeasureEndTaskHandle = new SingleMeasureEndTaskHandle(new SingleMeasureEndTaskHandle.SingalMeasureEndParameter(request, baseView, Constant.SingleMeasureStart, map, NetTaskSchedule));
        singleMeasureEndTaskHandle.start();
    }

    public void SingleMeasureEnd() {
        fileWork.pause();
        CancelSingleMeasureTransactions();
    }

    /**
     * IQ保存
     *
     * @param time
     */
    public void IQDataSave(long time, IBaseView baseView) {
        if (!map.containsKey(Constant.IQSave)) {
            IQSaveTransaction iqSaveTransaction = (IQSaveTransaction) Factory.getInstance().createTransaction(Constant.IQSave, NetTaskSchedule, baseView);
            iqSaveTransaction.setTime(time);
            NetTaskSchedule.addTransaction(iqSaveTransaction);
            map.put(Constant.IQSave, iqSaveTransaction);
        }
    }

    /**
     * 播放音频
     */
    public void AudioPlayerStart() {
        if (!map.containsKey(Constant.Audio)) {
            AudioPlayerTransaction audioPlayerTransaction = (AudioPlayerTransaction) Factory.getInstance().createTransaction(Constant.Audio, NetTaskSchedule, null);
            NetTaskSchedule.addTransaction(audioPlayerTransaction);
            map.put(Constant.Audio, audioPlayerTransaction);
        }
    }

    /**
     * 结束音频播放
     */
    public void AudioPlayerEnd() {
        CancelTransaction(Constant.Audio);
    }

    /**
     * ITU算法加入
     */
    public void ITUStart(short funcNum, double BW, double IQSampleRate, double xdbV, double betadbV, IBaseView baseView) {
        if (!map.containsKey(Constant.ITU) && map.containsKey(Constant.SingleMeasureStart)) {
            ITUTransaction ituTransaction = (ITUTransaction) Factory.getInstance().createTransaction(Constant.ITU, NetTaskSchedule, baseView);
            ituTransaction.setArithmetic(new ITUParamterArithmetic(funcNum, BW, IQSampleRate, xdbV, betadbV));
            NetTaskSchedule.addTransaction(ituTransaction);
            map.put(Constant.ITU, ituTransaction);
        }
    }

    public void ITUEnd() {
        CancelTransaction(Constant.ITU);
    }

    /**
     * 解调加入
     */
    public void MoudulationRecognitionStart(IBaseView baseView) {
        if (!map.containsKey(Constant.MR)) {
            ModulationRecognitionTransaction modulationRecognitionTransaction = (ModulationRecognitionTransaction) Factory.getInstance().createTransaction(Constant.MR, NetTaskSchedule, baseView);
            modulationRecognitionTransaction.setArithmetic(new ModulationrecognitionArithmetic<>());
            map.put(Constant.MR, modulationRecognitionTransaction);
            NetTaskSchedule.addTransaction(modulationRecognitionTransaction);
        }
    }

    public void MoudulationRecognitionEnd() {
        CancelTransaction(Constant.MR);
    }

    /**
     * 保存音频
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void AudioDataSave(IBaseView baseView) {
        if (map.containsKey(Constant.Audio) && !map.containsKey(Constant.AudioSave)) {
            AudioDataPersistenceFileTransaction audioDataPersistenceFileTransaction = (AudioDataPersistenceFileTransaction) Factory.getInstance().createTransaction(Constant.AudioSave, NetTaskSchedule, baseView);
            audioDataPersistenceFileTransaction.setDataParseForSave(o -> ((AudioData) ((DataPackage) o).Data.get("Audio")).Audio);
            map.put(Constant.ITU, audioDataPersistenceFileTransaction);
        }
    }

    public void AudioDataSaveEnd() {
        CancelTransaction(Constant.AudioSave);
    }

    /**
     * 保存单频测量数据
     *
     * @param fileName
     * @param parameter
     */
    public void SaveSingleMeasureData(IBaseView baseView, String fileName, Map<String, Object> parameter) {
        if (!map.containsKey(Constant.SingleMeasureSave)
//                && map.containsKey(Constant.SingleMeasureStart)x
        ) {
            SingleMeasureSaveTransaction singleMeasureSaveTransaction = (SingleMeasureSaveTransaction) Factory.getInstance().createTransaction(Constant.SingleMeasureSave, NetTaskSchedule, baseView);
            map.put(Constant.SingleMeasureSave, singleMeasureSaveTransaction);
            singleMeasureSaveTransaction.setFileName(fileName);
            singleMeasureSaveTransaction.setParamter(parameter);
            NetTaskSchedule.addTransaction(singleMeasureSaveTransaction);
        }
    }

    public void SaveSingleMeasureDataEnd() {
        CancelTransaction(Constant.SingleMeasureSave);
    }

    /**
     * 开始Pscan
     *
     * @param request
     */
    public void PSCANStart(Request request, IBaseView baseView) {
        fileWork.sendMessage(request);
        PSCANStartTaskHandle pscanStartTaskHandle = new PSCANStartTaskHandle(new PSCANStartTaskHandle.PSCANStartParameter(request, baseView, Constant.PScanStart, map, NetTaskSchedule));
        pscanStartTaskHandle.start();
    }

    public void PSCANStart(String request, IBaseView baseView) {
        fileWork.sendMessage(request);
        PScanTransaction pScanTransaction = (PScanTransaction) Factory.getInstance().createTransaction(Constant.PScanStart, NetTaskSchedule, baseView);
        NetTaskSchedule.addTransaction(pScanTransaction);
    }

    /**
     * 结束Pscan
     *
     * @param request
     */
    public void PSCANEnd(Request request, IBaseView baseView) {
        PSCANEndTaskHandle pscanEndTaskHandle = new PSCANEndTaskHandle(new PSCANEndTaskHandle.PSCANEndParameter(request, baseView, Constant.PScanStart, map, NetTaskSchedule));
        pscanEndTaskHandle.start();
    }

    public void PSCANEnd() {
        fileWork.pause();
        CancelTransaction(Constant.PScanStart);
        CancelPsTransactions();
    }

    /**
     * 开始跳频功能
     */
    public void FreqyencyHoppingStart(IBaseView baseView) {
        if (!map.containsKey(Constant.FreqyencyHopping)) {
            FrequencyHoppingTransaction frequencyHoppingTransaction = (FrequencyHoppingTransaction) Factory.getInstance().createTransaction(Constant.FreqyencyHopping,
                    NetTaskSchedule, baseView);
            frequencyHoppingTransaction.setArithmetic(new HopDectectorArithmetic());
            map.put(Constant.FreqyencyHopping, frequencyHoppingTransaction);
        }
    }

    /**
     * 结束跳频功能
     */
    public void FreqyencyHoppingEnd() {
        CancelTransaction(Constant.FreqyencyHopping);
    }

    /**
     *
     */
    public void SignalSortStart(float step, int smoothFrame, int dataType, boolean isLineThreshold, float lineThresholdValue, IBaseView baseView) {
        if (!map.containsKey(Constant.SignalSort)) {
            SignalSortTransaction signalSortTransaction = (SignalSortTransaction) Factory.getInstance().createTransaction(Constant.SignalSort, NetTaskSchedule, baseView);
            signalSortTransaction.setArithmetic(new SignalSortArithmetic(step, smoothFrame, dataType, isLineThreshold, lineThresholdValue));
            map.put(Constant.SignalSort, signalSortTransaction);
        }
    }

    public void SignalSortEnd() {
        fileWork.pause();
        CancelTransaction(Constant.SignalSort);
    }

    /**
     * 离散开始
     *
     * @param request
     * @param baseView
     */
    public void MSCANStart(Request request, IBaseView baseView) {
        MscanStartTaskHandle mscanStartTaskHandle = new MscanStartTaskHandle(new MscanStartTaskHandle.MscanStartParameter(request, DataTypeEnum.MSCAN.toString(), map, NetTaskSchedule, baseView));
        mscanStartTaskHandle.start();
    }

    public void MSCANStart(String request, IBaseView baseView) {
        fileWork.sendMessage(request);
        MscanTransaction mscanTransaction = (MscanTransaction) Factory.getInstance().createTransaction(Constant.MScanStart, NetTaskSchedule, baseView);
        mscanTransaction.taskSchedule.addTransaction(mscanTransaction);
    }

    /**
     * 离散结束
     *
     * @param request
     */
    public void MSCANEnd(Request request, IBaseView baseView) {
        MscanEndTaskHandle mscanEndTaskHandle = new MscanEndTaskHandle(new MscanEndTaskHandle.MscanEndParameter(request, DataTypeEnum.MSCAN.toString(), map, NetTaskSchedule, baseView));
        mscanEndTaskHandle.start();
    }

    /**
     * 本地的离散任务结束
     */
    public void MSCANEnd() {
        fileWork.pause();
        CancelTransaction(DataTypeEnum.MSCAN.toString());
    }


    /**
     * 开启多路ddc功能
     *
     * @param request
     */
    public void MultipathDDCStart(Request request, IBaseView baseView) {
        MultipathDDCStartTaskHandle multipathDDCStartTaskHandle = new MultipathDDCStartTaskHandle(new MultipathDDCStartTaskHandle.MultipathDDCStartParameter(request, Constant.MultiSignalStart, map, NetTaskSchedule, baseView));
        multipathDDCStartTaskHandle.start();
    }

    public void MultipathDDCStart(String request, IBaseView baseView) {
        MultiTransaction multiTransaction = (MultiTransaction) Factory.getInstance().createTransaction(Constant.MultiSignalStart, NetTaskSchedule, baseView);
        NetTaskSchedule.addTransaction(multiTransaction);
        fileWork.sendMessage(request);
    }

    /**
     * 结束多路ddc
     *
     * @param request
     */
    public void MultipathDDCEnd(Request request, IBaseView baseView) {
        MultipathDDCEndTaskHandle mscanEndTaskHandle = new MultipathDDCEndTaskHandle(new MultipathDDCEndTaskHandle.MultipathDDCEndParameter(request, baseView, Constant.MultiSignalStart, map, NetTaskSchedule));
        mscanEndTaskHandle.start();
    }

    public void MultipathDDCEnd() {
        fileWork.pause();
        CancelTransaction(Constant.MultiSignalStart);
    }

    /**
     * 开启跳频功能
     *
     * @param request
     * @param freqStep
     */
    public void HopDectectorStart(Request request, IBaseView baseView, double freqStep) {
        HopDectectorStartTaskHandle hopDectectorStartTaskHandle = new HopDectectorStartTaskHandle(new HopDectectorStartTaskHandle.HopDectectorStartParameter(request, Constant.HopDectectorStart, map, NetTaskSchedule, freqStep, baseView));
        hopDectectorStartTaskHandle.start();
    }

    /**
     * 结束跳频功能
     *
     * @param request
     */
    public void HopDectectorEnd(Request request, IBaseView baseView) {
        HopDectectorEndTaskHandle hopDectectorEndTaskHandle = new HopDectectorEndTaskHandle(new HopDectectorEndTaskHandle.HopDectectorEndParameter(request, Constant.HopDectectorStart, map, NetTaskSchedule, baseView));
        hopDectectorEndTaskHandle.start();
    }

    /**
     * 取消单频相关任务
     */
    private void CancelSingleMeasureTransactions() {
        CancelTransaction(Constant.SingleMeasureStart);
        CancelTransaction(Constant.IQSave);
        CancelTransaction(Constant.MR);
        CancelTransaction(Constant.Audio);
        CancelTransaction(Constant.AudioSave);
        CancelTransaction(Constant.ITU);
        CancelTransaction(Constant.SingleMeasureSave);
    }

    /**
     * 取消频段扫描相关任务
     */
    private void CancelPsTransactions() {
        CancelTransaction(Constant.PScanStart);
        CancelTransaction(Constant.FreqyencyHopping);
        CancelTransaction(Constant.SignalSort);
    }

    /**
     * 取消特定任务
     *
     * @param key
     */
    private void CancelTransaction(String key) {
        if (map.containsKey(key)) {
            map.get(key).cancelSelf();
            map.remove(key);
        }
    }
}

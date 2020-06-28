package com.huari.Presenter.Transactions.CommonTransactions;

import com.alibaba.fastjson.JSONWriter;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Fragment.UIinterface.SingleMeasureUI;
import com.huari.Presenter.Tools.FileTools;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;
import com.huari.Presenter.entity.Constant;
import com.huari.Presenter.entity.dataPersistence.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

public class SerializableDataSaveTransaction<T, M> extends Transaction<T, M> {
    WeakReference<IBaseView> baseViewWeakReference;
    LinkedBlockingDeque<Data> queue;
    Map<String, Object> paramter;
    JSONWriter writer;
    boolean saveFlag;
    int interval = 0;
    String fileName;
    long startTime;
    long time = 0;
    int delay = 0;
    File file;

    public SerializableDataSaveTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, String name, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        baseViewWeakReference = new WeakReference<>(baseView);
        fileName = name;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setParamter(Map<String, Object> paramter) {
        this.paramter = paramter;
    }

    @Override
    public T perform(T t) {
        Data data = new Data();
        data.t = t;
        if (time == 0) {
            time = System.currentTimeMillis();
            startTime = time;
        } else {
            long timeTmp = System.currentTimeMillis();
            delay = (int) (timeTmp - time);
            interval = interval + delay;
            if (interval > 500) {
                ((SingleMeasureUI)baseViewWeakReference.get()).onSave(true, timeTmp - startTime);
                interval = 0;
            }
            time = timeTmp;
        }
        data.delay = delay;
        data.type = eventType;
        queue.offer(data);
        return t;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (saveFlag) {
                try {
                    writer.writeValue(queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public boolean handle(M m) {
        return false;
    }

    @Override
    public void beCancel() {
        super.beCancel();
        saveFlag = false;
        writer.endArray();
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((SingleMeasureUI)baseViewWeakReference.get()).onSave(true, 0);
        ((SingleMeasureUI)baseViewWeakReference.get()).onSave(false, 0);
        queue.clear();
        queue = null;
    }

    @Override
    public void beAdd() {
        super.beAdd();
        queue = new LinkedBlockingDeque<>();
        saveFlag = true;
        try {
            file = new File(FileTools.getFileNameAccordDate(Constant.SINGLE_MEASURE_FLAG, true));
            file.delete();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            writer = new JSONWriter(new FileWriter(file));
            writer.startArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        taskSchedule.IOexecutorService.execute(runnable);
    }

    @Override
    public void work(T t) {
        super.work(t);
    }
}

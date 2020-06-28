package com.huari.Presenter.Impl;

import com.alibaba.fastjson.JSONReader;
import com.huari.Presenter.Interface.DataWork;
import com.huari.Presenter.Interface.DataWorkCallback;
import com.huari.Presenter.entity.dataPersistence.Data;

import java.io.File;
import java.io.FileReader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileWorkImpl implements DataWork<String> {
    DataWorkCallback dataWorkCallback;
    boolean parseFlag;
    boolean pause;
    String fileName = null;
    Lock lock;

    public FileWorkImpl() {
        lock = new ReentrantLock();
    }

    @Override
    public void initialize(DataWorkCallback dataWorkCallback) {
        parseFlag = true;
        pause = false;
        this.dataWorkCallback = dataWorkCallback;
    }

    @Override
    public void resume() {
        pause = false;
        lock.notifyAll();
    }

    @Override
    public void pause() {
        pause = true;
    }

    @Override
    public void close() {
        parseFlag = false;
        dataWorkCallback = null;
    }

    @Override
    public void sendMessage(String t) {
        fileName = t;
        pause = false;
        parseFlag = true;
        File file = new File(t);
        if (file.exists()) parse(file);
    }

    public void parse(File file) {
        if (file != null && file.exists()) {
            try {
                JSONReader reader = new JSONReader(new FileReader(file));
                reader.startArray();
                while (reader.hasNext() && parseFlag) {
                    if (pause) lock.wait();
                    Data data = reader.readObject(Data.class);
                    Thread.sleep(data.delay);
                    dataWorkCallback.Databack(data.t);
                }
                reader.endArray();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

package com.huari.Presenter.Impl;

import com.cdhuari.entity.DataPackage;
import com.huari.Presenter.Interface.DataWork;
import com.huari.Presenter.Interface.DataWorkCallback;
import com.huari.Presenter.entity.HeaderParameter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HistoryWorkImpl implements DataWork<Integer> {
    BlockingDeque<DataPackage> queue = new LinkedBlockingDeque<>(30);
    ExecutorService executorService = Executors.newScheduledThreadPool(5);
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    ObjectInputStream objectInputStream;
    DataWorkCallback dataWorkCallback;
    FileInputStream fileInputStream;
    HeaderParameter headerParameter;
    volatile int lockInt = 0;
    boolean parseFlg = false;//标志是否正在解析
    boolean parseContinue = true;
    String filename;
    int[] delayTime;
    int postion = 0;
    int startPosition = 0;
    int length;

    public HistoryWorkImpl(String filename) {
        this.filename = filename;
    }

    @Override
    public void initialize(final DataWorkCallback dataWorkCallback) {
        if (this.dataWorkCallback != null) {
            lockInt = 0;
            condition.signalAll();
            return;
        }
        this.dataWorkCallback = dataWorkCallback;
        openFileStream();
        startParseFormPosition(startPosition);
        startCallbackData();
    }

    @Override
    public void resume() {

    }

    private void openFileStream() {
        try {
            fileInputStream = new FileInputStream(filename);
            objectInputStream = new ObjectInputStream(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCallbackData() {
        executorService.execute(() -> {
            while (parseFlg || !queue.isEmpty()) {
                if (lockInt == 1) {
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(delayTime[delayTime[postion]]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executorService.execute(() -> {
                    try {
                        dataWorkCallback.Databack(queue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                postion++;
            }
        });
    }

    private void startParseFormPosition(final int position) {
        executorService.execute(() -> {
            try {
                parseFile(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void parseFile(int position) throws IOException, ClassNotFoundException {
        int i = 0;
        this.postion = position;
        parseFlg = true;
        parseContinue = true;
        while (fileInputStream.available() > 0) {
            if (!parseContinue) return;
            if (i == 0) {
                delayTime = (int[]) objectInputStream.readObject();
                length = delayTime.length;
                System.out.println("读取~");
            } else if (i == 1) {
                headerParameter = (HeaderParameter) objectInputStream.readObject();
            } else {
                if (i < position) {
                    objectInputStream.readObject();
                } else {
                    queue.add((DataPackage) objectInputStream.readObject());
                }
                System.out.println("读取~");
            }
            i++;
        }
        parseContinue = false;
        parseFlg = false;
    }

    @Override
    public void pause() {
        lockInt = 1;
    }

    @Override
    public void close() {
        dataWorkCallback = null;
        executorService.shutdownNow();
        try {
            fileInputStream.close();
            objectInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        queue.clear();
    }

    @Override
    public void sendMessage(Integer t) {
        DataWorkCallback dataWorkCallback1 = dataWorkCallback;
        parseContinue = false;
        close();
        startPosition = t;
        initialize(dataWorkCallback1);
    }
}

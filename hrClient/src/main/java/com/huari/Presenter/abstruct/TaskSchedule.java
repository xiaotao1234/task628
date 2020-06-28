package com.huari.Presenter.abstruct;


import android.util.Log;

import com.cdhuari.entity.DataPackage;
import com.huari.Presenter.Interface.DataWork;
import com.huari.Presenter.Transactions.CommonTransactions.Factory;
import com.huari.Presenter.entity.CallbackData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskSchedule<T, M> {
    public Factory factory;
    public ExecutorService CommonexecutorService;
    public TreeMap<Integer, Transaction> transactionTreeMap;
    public ExecutorService IOexecutorService;
    public ExecutorService UIexecutorService;
    public DataWork dataWork;//对通讯层封装的接口类型
    public DataWork dataWorkFile;
    public Lock lock;

    public TaskSchedule(DataWork dataWork, DataWork dataWorkFile, ExecutorService commonexecutorService, ExecutorService IOexecutorService, ExecutorService UiexecutorService, Factory factory) {
        this.dataWork = dataWork;
        this.dataWorkFile = dataWorkFile;
        this.factory = factory;
        this.CommonexecutorService = commonexecutorService;
        this.IOexecutorService = IOexecutorService;
        this.UIexecutorService = UiexecutorService;
        transactionTreeMap = new TreeMap<>();
        lock = new ReentrantLock();
    }

    public void startNetWork(DataWork dataWork) {
        this.dataWork = dataWork;
        dataWork.initialize(o -> {
            final CallbackData callbackData = (CallbackData) o;
            final DataPackage dataPackage = (DataPackage) callbackData.t;
            Log.d("Reply", "数据类型:" + dataPackage.DataType);
            try {
                lock.lock();
                Iterator iterator = transactionTreeMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Transaction transaction = ((Map.Entry<Integer, Transaction>) iterator.next()).getValue();
                    if (transaction.handle(dataPackage.DataType)) {//类型匹配
                        CommonexecutorService.execute(() -> {
                            DataPackage dataPackageCopy = new DataPackage();
                            dataPackageCopy.DataType = dataPackage.DataType;
                            dataPackageCopy.Data.putAll(dataPackage.Data);
                            transaction.work(dataPackageCopy);
                        });
                    }
                }
            } finally {
                lock.unlock();
            }
        });
    }

    public void startFileWork(DataWork dataWork) {
        this.dataWorkFile = dataWork;
        dataWork.initialize(o -> {
            final CallbackData callbackData = (CallbackData) o;
            final DataPackage dataPackage = (DataPackage) callbackData.t;
            Log.d("Reply", "数据类型:" + dataPackage.DataType);
            try {
                lock.lock();
                Iterator iterator = transactionTreeMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Transaction transaction = ((Map.Entry<Integer, Transaction>) iterator.next()).getValue();
                    if (transaction.handle(dataPackage.DataType)) {//类型匹配
                        CommonexecutorService.execute(() -> {
                            DataPackage dataPackageCopy = new DataPackage();
                            dataPackageCopy.DataType = dataPackage.DataType;
                            dataPackageCopy.Data.putAll(dataPackage.Data);
                            transaction.work(dataPackageCopy);
                        });
                    }
                }
            } finally {
                lock.unlock();
            }
        });
    }

    public void endNetWork() {//error
        try {
            lock.lock();
            transactionTreeMap.clear();
        } finally {
            lock.unlock();
        }
    }

    public void endFileWork() {
        clearTaskExeceptIdGroup(Arrays.asList(0, -1));
    }

    public void sendCommandNet(T t) {
        dataWork.sendMessage(t);
    }

    public void sendCommandFile(T t) {
        dataWorkFile.sendMessage(t);
    }

    public void clearTaskExeceptIdGroup(List<Integer> list) {
        for (Map.Entry<Integer, Transaction> transaction : transactionTreeMap.entrySet()) {
            if (!list.contains(transaction.getKey())) {
                transactionTreeMap.remove(transaction.getKey());
            }
        }
    }

    public void addTransaction(Transaction transaction) {
        try {
            lock.lock();
            transaction.beAdd();
            List<String> list = null;
            if (transaction.preEventType != null) list = new ArrayList<>(transaction.preEventType);
            if (transactionTreeMap.containsKey(transaction.taskSetNumber)) {
                transactionTreeMap.put(transaction.taskSetNumber, add(transactionTreeMap.get(transaction.taskSetNumber), transaction, transaction.preEventType == null ? null : new ArrayList<String>(transaction.preEventType)));
            } else {
                transactionTreeMap.put(transaction.taskSetNumber, add(null, transaction, transaction.preEventType == null ? null : new ArrayList<String>(transaction.preEventType)));
            }
            transaction.preEventType = list;
        } finally {
            lock.unlock();
        }
    } //添加任务


    public void cancelTransation(Transaction transaction) {
        try {
            lock.lock();
            if (transactionTreeMap.containsKey(transaction.taskSetNumber)) {
                if (transactionTreeMap.get(transaction.taskSetNumber).next == null) {
                    transactionTreeMap.remove(transaction.taskSetNumber);
                } else {
                    Transaction transaction1 = transactionTreeMap.get(transaction.taskSetNumber);
                    if (transaction1 != null) {
                        Transaction transaction2 = delete(transaction1, transaction);
                        if (transaction2 == null) {
                            transactionTreeMap.remove(transaction.taskSetNumber);
                        } else {
                            transactionTreeMap.put(transaction.taskSetNumber, transaction2);
                        }
                    }
                }
            }
            transaction.beCancel();
        } finally {
            lock.unlock();
        }
    }//取消某个任务

    public void clearAllTransaction() {
        try {
            lock.lock();
            Iterator iterator = transactionTreeMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Transaction transaction = (Transaction) iterator.next();
                while (transaction != null) {
                    transaction.beCancel();
                    transaction = transaction.next;
                }
            }
            transactionTreeMap.clear();
        } finally {
            lock.unlock();
        }
    }//删除所有任务

    public void addTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            addTransaction(transaction);
        }
    }//批量的进行任务的添加

    public Transaction delete(Transaction transaction, Transaction target) {
        if (transaction == null) {
            return null;
        }
        if (transaction == target) {//找到任务，直接将任务从链表删除
            return transaction.next;
        }
        List<Integer> list = target.preEventType;
        if (list.contains(transaction.eventType)) {//若当前节点是目标任务的前驱任务，则将当前任务的引用计数减1
            transaction.ReferenceCount--;
            if (transaction.ReferenceCount == 0) {//若当前任务的引用数减小到了0，那么 代表该任务已经失去了运行的意义，直接从链中删除该任务。
                transaction = delete(transaction.next, target);
            } else {
                transaction.next = delete(transaction.next, target);
            }
        }
        return transaction;
    }

    public Transaction add(Transaction transaction, Transaction target, List<String> list) {
        if (transaction == null) {
            Transaction transaction1 = target;
            if (list != null && list.size() != 0) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    Transaction transaction2 = Factory.getInstance().createTransaction(list.get(i), this, null);
                    ++transaction2.ReferenceCount;
                    transaction2.beAdd();
                    transaction2.next = transaction1;
                    transaction1 = transaction2;
                }
            }
            return transaction1;
        }
        if (target.preEventType != null && target.preEventType.contains(transaction.eventType)) {
            transaction.ReferenceCount++;
            list.remove(list.indexOf(transaction.eventType));
        }
        transaction.next = add(transaction.next, target, list);
        return transaction;
    }
}

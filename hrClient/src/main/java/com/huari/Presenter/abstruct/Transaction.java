package com.huari.Presenter.abstruct;

import android.util.Log;

import androidx.annotation.Nullable;

import com.huari.Presenter.entity.Logg;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Transaction<T, M> {

    private static AtomicInteger nextHashCode = new AtomicInteger();

    private final int threadLocalHashCode = nextHashCode();//任务的唯一标识号

    public String eventType;//任务类型

    public int taskSetNumber;//任务组，数字越小优先级越高，任务组越先被调度执行

    public List<String> preEventType;//前驱事件，用于建立整个事件族的拓扑结构

    public int ReferenceCount;//任务被引用数

    public Transaction next;

    public TaskSchedule taskSchedule;//持有的任务调度器的引用

    public abstract T perform(T t);//业务处理

    public void beCancel() {//
        Log.d("taskSchedule", "callback");
    }

    public void beAdd() {

    }

    public abstract boolean handle(M m);

    public Transaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule) {
        this.eventType = eventType;
        this.taskSetNumber = taskSetNumber;
        this.preEventType = preEventType;
        this.taskSchedule = taskSchedule;
        ReferenceCount = referenceCount;
    }

    private static int nextHashCode() {
        return nextHashCode.getAndAdd(1);
    }

    public void cancelSelf() {
        Log.d("cancelself", String.valueOf(1));
        taskSchedule.cancelTransation(this);
        preEventType = null;
        next = null;
        taskSchedule = null;
    }

    public void work(T t) {
        long time = System.currentTimeMillis();
        T t1 = perform(t);
        Log.d(String.valueOf(Logg.DataType), Logg.DataType + "|" + threadLocalHashCode + "|" + (System.currentTimeMillis() - time));//对任务耗时进行追踪
        if (next != null) {
            next.work(t1);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return ((Transaction) obj).threadLocalHashCode == this.threadLocalHashCode;
    }

    @Override
    public int hashCode() {
        return threadLocalHashCode;
    }

}













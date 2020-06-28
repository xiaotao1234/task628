package com.huari.Presenter.Tools;

import com.cdhuari.entity.DataPackage;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BufferForFixCapacity {
    LinkedList<DataPackage> queue;
    Lock lock;
    Condition empty;
    int capital = 50;

    public BufferForFixCapacity(int maxSize) {
        capital = maxSize;
        lock = new ReentrantLock();
        queue = new LinkedList<>();
        empty = lock.newCondition();
    }

    public BufferForFixCapacity() {
        lock = new ReentrantLock();
        queue = new LinkedList<>();
        empty = lock.newCondition();
    }

    public void add(DataPackage dataPackage) {
        lock.lock();
        if (queue.size() >= capital) {
            queue.removeLast();
        }
        queue.push(dataPackage);
        empty.signalAll();
        lock.unlock();
    }

    public DataPackage get() {
        lock.lock();
        while (queue.isEmpty()) {
            try {
                empty.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        DataPackage dataPackage = queue.pop();
        lock.unlock();
        return dataPackage;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

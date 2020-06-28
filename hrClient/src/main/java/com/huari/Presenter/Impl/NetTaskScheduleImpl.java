package com.huari.Presenter.Impl;

import com.huari.Presenter.Interface.DataWork;
import com.huari.Presenter.Transactions.CommonTransactions.Factory;
import com.huari.Presenter.abstruct.TaskSchedule;

import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class NetTaskScheduleImpl extends TaskSchedule {

    private static volatile NetTaskScheduleImpl single;

    //    Map<DataTypeEnum, Transaction> map =   new HashMap<>();  //若发现任务数大而导致了任务调度的速度急剧下降，则应改为以任务类型为索引来提高任务的检索速度，但在当前单任务系统下不需要这样。

    private NetTaskScheduleImpl(DataWork dataWork, ExecutorService commonexecutorService, ExecutorService IOexecutorService, ExecutorService UiexecutorService, Factory factory) {
        super(dataWork, null, commonexecutorService, IOexecutorService, UiexecutorService, factory);
        this.dataWork = dataWork;
        this.factory = factory;
        this.CommonexecutorService = commonexecutorService;
        this.IOexecutorService = IOexecutorService;
        this.UIexecutorService = UiexecutorService;
        transactionTreeMap = new TreeMap<>();
        lock = new ReentrantLock();
    }

    /**
     * @param
     * @return
     * @descriptiond单例对象
     */
    public static NetTaskScheduleImpl getSingleNetWork(Builder builder) {
        if (single == null) {
            synchronized (NetTaskScheduleImpl.class) {
                if (single == null) {
                    single = builder.build();
                }
                return single;
            }
        }
        return single;
    }

    public static final class Builder {
//        private static volatile NetTaskScheduleImpl single;
//        TreeMap<Integer, Transaction> transactionTreeMap; //任务的集合
        static ExecutorService CommonexecutorService;
        static ExecutorService IOexecutorService;
        static ExecutorService UiexecutorService;
        DataWork dataWork;//对通讯层封装的接口类型
        Factory factory;

        public Builder(DataWork dataWork) {
            this.dataWork = dataWork;
        }

        public NetTaskScheduleImpl.Builder transactionFactory(Factory factory) {
            this.factory = factory;
            return this;
        }

        public NetTaskScheduleImpl.Builder setCommonexecutorService(ExecutorService executorService) {
            CommonexecutorService = executorService;
            return this;
        }

        public NetTaskScheduleImpl.Builder setIOexecutorService(ExecutorService executorService) {
            IOexecutorService = executorService;
            return this;
        }

        public NetTaskScheduleImpl.Builder setNetwork(DataWork network) {
            this.dataWork = network;
            return this;
        }

        public NetTaskScheduleImpl build() {
            if (dataWork == null) {
                dataWork = new NetWorkImpl();
            }
            if (CommonexecutorService == null) {
                CommonexecutorService = Executors.newScheduledThreadPool(8);
            }
            if (IOexecutorService == null) {
                IOexecutorService = Executors.newScheduledThreadPool(5);
            }
            if (UiexecutorService == null) {
                UiexecutorService = Executors.newCachedThreadPool();
            }
            return new NetTaskScheduleImpl(dataWork, CommonexecutorService, IOexecutorService, UiexecutorService, factory);
        }
    }
}

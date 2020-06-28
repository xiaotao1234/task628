package com.huari.Presenter.BusinessCore;

import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;

import java.util.Map;

public class BaseTaskParameter {
    String taskName;
    Map<String, Transaction> map;
    TaskSchedule taskSchedule;

    public BaseTaskParameter(String taskName, Map<String, Transaction> map, TaskSchedule taskSchedule) {
        this.taskName = taskName;
        this.map = map;
        this.taskSchedule = taskSchedule;
    }

    public void destory(){}
}

package com.huari.Presenter.BusinessCore;

import com.huari.Presenter.Transactions.CommonTransactions.CallbackTransaction;
import com.huari.Presenter.entity.Constant;

public abstract class TaskHandle {
    BaseTaskParameter baseTaskParameter;

    public TaskHandle(BaseTaskParameter baseTaskParameter) {
        this.baseTaskParameter = baseTaskParameter;
    }

    /**
     * 请求成功处理
     */
    public abstract void requestSuccess();

    /**
     * 请求失败处理
     */
    public abstract void requestFailed();

    /**
     * 请求返回共有处理流程
     */
    public abstract void callbackCommon(String s);

    /**
     * 回调任务的添加处理及以后的处理
     */
    public abstract void afterTaskAdd();

    /**
     * 用完后释放
     */
    public void destory() {

    }

    /**
     * 对回调状态结果进行判断，在判断条件改变时，如返回结果为连接状态，需要重写此方法
     *
     * @param s
     * @return
     */
    public boolean decide(String s) {
        return s.equals(Constant.CallbackReeslt_Success);
    }

    /**
     * 模板方法
     */
    public void start() {
        if (!baseTaskParameter.map.containsKey(baseTaskParameter.taskName + Constant.Callback)) {
            CallbackTransaction callbackTransaction = new CallbackTransaction(baseTaskParameter.taskName + Constant.Callback, Constant.CallBackID, null, 1, s -> {
                if (decide(s)) requestSuccess();
                else requestFailed();
                CancelTransaction(baseTaskParameter.taskName + Constant.Callback);
                callbackCommon(s);
            }, baseTaskParameter.taskSchedule);
            baseTaskParameter.taskSchedule.addTransaction(callbackTransaction);
            baseTaskParameter.map.put(baseTaskParameter.taskName + Constant.Callback, callbackTransaction);
            afterTaskAdd();
        }
    }

    public void CancelTransaction(String key) {
        if (baseTaskParameter.map.containsKey(key)) {
            baseTaskParameter.map.get(key).cancelSelf();
            baseTaskParameter.map.remove(key);
        }
    }
}

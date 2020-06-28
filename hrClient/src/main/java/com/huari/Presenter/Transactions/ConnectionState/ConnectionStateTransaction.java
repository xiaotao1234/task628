package com.huari.Presenter.Transactions.ConnectionState;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.huari.Fragment.UIinterface.ConnectionStateUI;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.UI.Listener.ConnectionStateListener;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;

import java.lang.ref.WeakReference;
import java.util.List;

public class ConnectionStateTransaction<T, M> extends Transaction<T, M> {
    WeakReference<IBaseView> iBaseViewWeakReference;
    ConnectionStateListener connectionStateListener;

    public void setConnectionStateListener(ConnectionStateListener connectionStateListener) {
        this.connectionStateListener = connectionStateListener;
    }

    public ConnectionStateTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, IBaseView iBaseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        iBaseViewWeakReference = new WeakReference<>(iBaseView);
    }

    @Override
    public T perform(T t) {
        if ((connectionStateListener != null) && ((DataPackage) t).Data.containsKey(DataTypeEnum.ConnectionState.toString())) {
            String result = (String) ((DataPackage) t).Data.get(DataTypeEnum.ConnectionState.toString());
            if (iBaseViewWeakReference != null)
                ((ConnectionStateUI) iBaseViewWeakReference.get()).ConnectionStateCallback(result);
            connectionStateListener.connectionListener(result);
        }
        return t;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.ConnectionState;
    }
}

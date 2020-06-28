package com.huari.Presenter.Transactions.DeviceState;

import com.cdhuari.entity.DataPackage;
import com.cdhuari.entity.DataTypeEnum;
import com.cdhuari.entity.DeviceInfo;
import com.huari.Fragment.UIinterface.DeviceStateUI;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.abstruct.TaskSchedule;
import com.huari.Presenter.abstruct.Transaction;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class DeviceStateTransaction<T, M> extends Transaction<T, M> {
    WeakReference<IBaseView> baseViewWeakReference;
    DeviceInfo deviceInfo = null;

    public void setOnDeviceStateListener(IBaseView baseView) {
        baseViewWeakReference = new WeakReference<>(baseView);
        if (baseViewWeakReference != null)
            ((DeviceStateUI) baseView).DeviceStateCallback(deviceInfo);
    }

    public DeviceStateTransaction(String eventType, int taskSetNumber, List<String> preEventType, int referenceCount, TaskSchedule taskSchedule, IBaseView baseView) {
        super(eventType, taskSetNumber, preEventType, referenceCount, taskSchedule);
        this.baseViewWeakReference = new WeakReference<>(baseView);
    }

    @Override
    public T perform(T t) {
        Map map = ((DataPackage) t).Data;
        if (map.containsKey(DataTypeEnum.DeviceInfo.toString())) {
            DeviceInfo deviceInfo = (DeviceInfo) map.get(DataTypeEnum.DeviceInfo.toString());
            this.deviceInfo = deviceInfo;
            if (baseViewWeakReference != null)
                ((DeviceStateUI) baseViewWeakReference.get()).DeviceStateCallback(deviceInfo);
        }
        return null;
    }

    @Override
    public boolean handle(M m) {
        return m == DataTypeEnum.DeviceInfo;
    }
}

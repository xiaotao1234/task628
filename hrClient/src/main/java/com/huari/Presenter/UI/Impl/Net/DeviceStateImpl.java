package com.huari.Presenter.UI.Impl.Net;

import com.cdhuari.entity.DeviceInfo;
import com.huari.Fragment.UIinterface.DeviceStateUI;
import com.huari.Fragment.UIinterface.IBaseView;
import com.huari.Presenter.BusinessCore.BusinessCoreNet;

import java.lang.ref.WeakReference;

public class DeviceStateImpl implements DeviceStateUI {

    WeakReference<IBaseView> baseViewWeakReference;

    public DeviceStateImpl(IBaseView baseView) {
        baseViewWeakReference = new WeakReference<>(baseView);
    }

    @Override
    public void DeviceStateCallback(DeviceInfo deviceInfo) {
        BusinessCoreNet.getInstanceNet().setOnDeviceStateListener((IBaseView) baseViewWeakReference.get());
    }
}

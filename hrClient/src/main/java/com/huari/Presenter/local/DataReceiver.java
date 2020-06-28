package com.huari.Presenter.local;

import com.cdhuari.entity.DataPackage;

public interface DataReceiver {
    void dataArrive(DataPackage data);
}
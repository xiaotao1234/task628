package com.huari.Presenter.entity.UI;

import com.cdhuari.entity.DataPackage;

public class HistoryBackData {
    DataPackage dataPackage;

    public HistoryBackData(DataPackage dataPackage, int position) {
        this.dataPackage = dataPackage;
        this.position = position;
    }

    int position;
}

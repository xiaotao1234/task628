package com.huari.Presenter.Impl;

import com.cdhuari.entity.AudioData;
import com.cdhuari.entity.DataPackage;
import com.huari.Presenter.Interface.DataParseForSave;

public class DataParseForSaveVoice implements DataParseForSave<byte[], DataPackage> {
    @Override
    public byte[] parse(DataPackage dataPackage) {
        return ((AudioData)dataPackage.Data.get("Audio")).Audio;
    }
}

package com.huari.Presenter.Interface;

import com.cdhuari.entity.DataPackage;

import java.io.File;

public interface PFscanPresenter {
    public boolean startScan(DataPackage dataPackage);//开始频段扫描

    public boolean endScan(DataPackage dataPackage);//结束频段扫描

    public boolean startRecord();//开始记录数据

    public boolean endRecord();//停止记录数据

    public boolean saveTemplate(byte[] bytes);//保存模板

    public String[] getTemplateList();//获取模板列表

    public byte[] getTemplateData(File file);//获取具体的模板数据
}

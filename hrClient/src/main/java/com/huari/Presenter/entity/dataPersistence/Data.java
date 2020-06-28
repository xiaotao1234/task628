package com.huari.Presenter.entity.dataPersistence;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

public class Data<T> {
    @JSONField(name = "data")
    public T t;
    @JSONField(name = "delay")
    public int delay;
    @JSONField(name = "type")
    public String type;
    @JSONField(name = "paramter")
    public Map<String, Object> paramter;
}
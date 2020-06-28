package com.huari.Presenter.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;

public class Request {
    @JSONField(name = "ReqType_Str")
    public String type;
    @JSONField(name = "Param_List")
    public List<Map<String, Object>> list;
}

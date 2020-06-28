package com.huari.Presenter.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;

public class DPRequestParamter {
    @JSONField(
            name = "ReqType_Str",
            deserialize = false
    )
    public String retype;
    @JSONField(
            name = "Param_List",
            deserialize = false
    )
    public List<Map<String, Object>> reMap;
}

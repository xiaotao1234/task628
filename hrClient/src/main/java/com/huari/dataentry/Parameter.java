package com.huari.dataentry;

import java.io.Serializable;

public class Parameter implements Serializable, Cloneable {

    public String name;         // 参数名称（英文）
    public float maxValue;
    public float minValue;
    public String defaultValue; // 当前值
    public String displayType;  // 显示范畴
    public byte isAdvanced;
    public byte isEditable;
    public String dispname;     //参数显示名称（中文）

    public String[] enumValues = null;

    @Override
    public Object clone() throws CloneNotSupportedException {
        Parameter cloned = (Parameter) super.clone();
        return cloned;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public void setIsAdvanced(byte isAdvanced) {
        this.isAdvanced = isAdvanced;
    }

    public void setIsEditable(byte isEditable) {
        this.isEditable = isEditable;
    }

    public void setEnumValues(String[] enumValues) {
        this.enumValues = enumValues;
    }

    public void setDispname(String dispname) {
        this.dispname = dispname;
    }

    public String getName() {

        return name;
    }

    public String getDispName() {
        return dispname;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDisplayType() {
        return displayType;
    }

    public byte getIsAdvanced() {
        return isAdvanced;
    }

    public byte getIsEditable() {
        return isEditable;
    }

    public String[] getEnumValues() {
        return enumValues;
    }
}

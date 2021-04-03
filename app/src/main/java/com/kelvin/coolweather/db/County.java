package com.kelvin.coolweather.db;

import org.litepal.crud.DataSupport;

public class County extends DataSupport {
    private int id;
    private String countyName; //记录县的名字
    private String weatherId; //记录县所对应的天气id
    private int cityId; //记录当前县所属市的id值
}

package com.kelvin.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * JSON格式的basic
 * "basic":{"city":"苏州",
 *          "id":CN101190401,
 *          "update":{"loc":"2016-08-08 21:58"}}
 */
public class Basic {
    @SerializedName("city")   //使用了@SerializedName注解的方式来让JSON字段和Java字段之间建立映射关系
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}

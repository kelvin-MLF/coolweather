package com.kelvin.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * JSON格式的suggestion
 * "suggestion":{
 *          "comf":{"txt":"天气不舒适"},
 *          "cw":{"txt":"不宜洗车"},
 *          "sport":{"txt":"有降水"}}
 */
public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;
    @SerializedName("cw")
    public CarWash carWash;
    public Sport sport;

    public class Comfort {
        @SerializedName("txt")
        public String info;
    }

    public class CarWash {
        @SerializedName("txt")
        public String info;
    }

    public class Sport {
        @SerializedName("txt")
        public String info;
    }
}

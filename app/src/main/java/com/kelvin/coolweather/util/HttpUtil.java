package com.kelvin.coolweather.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

//与服务器进行交互
public class HttpUtil {
    //从服务器端获取数据
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
//        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS).readTimeout(60000,TimeUnit.MILLISECONDS).build();
        client.newCall(request).enqueue(callback);
    }
}

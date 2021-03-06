package com.kelvin.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.kelvin.coolweather.db.City;
import com.kelvin.coolweather.db.County;
import com.kelvin.coolweather.db.Province;
import com.kelvin.coolweather.gson.Weather;
import com.kelvin.coolweather.util.HttpUtil;
import com.kelvin.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Choose_AreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY =1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel ==LEVEL_PROVINCE){ //当当前选中级别为省
                    selectedProvince= provinceList.get(position);
                    queryCities(); //自定义方法 查询选中省内所有的市
                }else if (currentLevel==LEVEL_CITY){ //当当前选中级别为市
                    selectedCity = cityList.get(position);
                    queryCounties(); //自定义方法 查询选中市内所有的县
                }else if (currentLevel==LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity){
                        Intent intent =  new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() { //当按返回键时
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){ //当当前选中级别为县
                    queryCities(); //自定义方法 查询选中省内所有的市
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();//自定义方法 查询全国所有的省
                }
            }
        });
        queryProvinces(); //默认查询全国所有的省
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE); //返回按钮不可见
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address ="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address ="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for (County county :countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address,final String type) {
        showProgressDialog();//自定义显示进度对话框方法
        HttpUtil.sendOkHttpRequest(address, new Callback() { //向服务器发送请求

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException { //响应的数据会回调到onResponse()方法中
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){ //如果传入的是省
                    result = Utility.handleProvinceResponse(responseText); //使用自定义的Utility类中的对省JSON数据的解析方法
                }else if ("city".equals(type)) { //如果传入的是市
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());//使用自定义的Utility类中的对市JSON数据的解析方法
                }else if ("county".equals(type)){//如果传入的是区
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());//使用自定义的Utility类中的对市JSON数据的解析方法
                }
                if (result){
                    getActivity().runOnUiThread(() -> {
                        closeProgressDialog();//自定义关闭进度对话框方法
                        if ("province".equals(type)){
                            queryProvinces();
                        }else if ("city".equals(type)){
                            queryCities();
                        }else if ("county".equals(type)){
                            queryCounties();
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

}

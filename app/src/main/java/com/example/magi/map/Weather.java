package com.example.magi.map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//天气类（数据源为京东万象）
public class Weather {
    private  static String weather;

    //发送天气请求
    public static String getWeather(final String city){
        HttpURLConnection connection;
        try{
            URL url = new URL("https://way.jd.com/he/freeweather?city=" + city +"&appkey=3e5c10a453a26ef13a104e5b6e3fe1c0");
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            weather = ParseJsonObjectToJsonArray(response.toString());
            return weather;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //解析返回的天气Json数据
    private static String ParseJsonObjectToJsonArray(String jsonData) {
        try{
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject result = jsonObject.getJSONObject("result");
            JSONArray jsonArray = result.getJSONArray("HeWeather5");
            JSONObject jsonObject2 = jsonArray.getJSONObject(0);
            JSONObject now = jsonObject2.getJSONObject("now");
            JSONObject cond = now.getJSONObject("cond");
            JSONObject basic = jsonObject2.getJSONObject("basic");
            JSONObject update = basic.getJSONObject("update");
            String wea_vis = now.optString("vis");
            String wea_tmp = now.optString("tmp");
            String wea_text = cond.optString("txt");
            String wea_city = basic.optString("city");
            return "城市:" + wea_city + "，天气:" + wea_text + "，温度:" + wea_tmp + "℃，可见度:" + wea_vis;
        }
        catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }
}

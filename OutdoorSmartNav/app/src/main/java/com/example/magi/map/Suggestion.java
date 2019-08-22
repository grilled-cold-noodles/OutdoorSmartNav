package com.example.magi.map;

import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//地名建议类（百度提供信息）
public class Suggestion {
    private static String ak = "K3rIDlaBoyjTPiC5iw02uyFUYDnDOwZP";
    private static String sn;

    //发送搜索请求
    public static PoiList getSuggestion(String query, String region){
        HttpURLConnection connection;
        //构建参数对
        Map map = new LinkedHashMap<String, String>();
        map.put("query", query);
        map.put("region", region);
        map.put("output", "json");
        map.put("ak", ak);
        sn = SNCal.getSn(map, 1); //计算SN

        try{
            URL url = new URL("http://api.map.baidu.com/place/v2/suggestion?query=" + URLEncoder.encode(query) + "&region=" + URLEncoder.encode(region) +
                    "&output=json" + "&ak=" + ak + "&sn=" + sn);
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
            PoiList list = ParseJsonObjectToJsonArray(response.toString());
            return list;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //解析返回的Json数据
    private static PoiList ParseJsonObjectToJsonArray(String jsonData) {
        PoiList poiList = new PoiList();
        try{
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray result = jsonObject.getJSONArray("result");
            List<Storage> lst = new ArrayList<>();
            for(int i = 0; i < result.length(); i++){
                JSONObject item = result.getJSONObject(i);
                Storage storage = new Storage();
                storage.setName(item.getString("name"));
                storage.setAddress(item.getString("city") + item.getString("district"));
                JSONObject location = item.getJSONObject("location");
                storage.setLocation(new LatLng(location.getDouble("lat"), location.getDouble("lng")));
                lst.add(storage);
            }
            poiList.setCurrSize(result.length());
            poiList.setStorageList(lst);
            return poiList;
        }
        catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }
}

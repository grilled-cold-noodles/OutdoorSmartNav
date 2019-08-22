package com.example.magi.map;

import android.util.Log;

import java.net.URLEncoder;
import java.util.Map;

//SN计算类
public class SNCal {

    private static String SK = "ISIphtRblvn2Ehd3dD3GVA7t3BluiOhV";

    //获取SN
    public static String getSn(Map<?, ?> data, int type){
        String paramsStr = toQueryString(data);
        String wholeStr = "/geosearch/v3/nearby?" + paramsStr + SK;
        if(type == 1){
            wholeStr = "/place/v2/suggestion?" + paramsStr + SK;
        }
        try {
            String tempStr = URLEncoder.encode(wholeStr, "UTF-8");
            return MD5(tempStr);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //参数转码
    private static String toQueryString(Map<?, ?> data){
        StringBuilder queryString = new StringBuilder();
        try{
            for (Map.Entry<?, ?> pair : data.entrySet()) {
                queryString.append(pair.getKey() + "=");
                String ss[] = pair.getValue().toString().split(",");
                String bb[] = pair.getValue().toString().split(":");
                if(ss.length > 1){
                    for(String s : ss){
                        queryString.append(URLEncoder.encode(s, "UTF-8") + ",");
                    }
                    queryString.deleteCharAt(queryString.length()-1);
                    queryString.append("&");
                }
                else{
                    if(bb.length > 1){
                        for(String b : bb){
                            queryString.append(URLEncoder.encode(b, "UTF-8") + ":");
                        }
                        queryString.deleteCharAt(queryString.length()-1);
                        queryString.append("&");
                    }
                    else {
                        queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8") + "&");
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }

    //MD5计算
    private static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

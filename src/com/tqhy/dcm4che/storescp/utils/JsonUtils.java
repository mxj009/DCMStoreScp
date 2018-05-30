package com.tqhy.dcm4che.storescp.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * json解析工具类
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class JsonUtils {

    public static <T> T json2Obj(String json, Class<T> type) {
        return new Gson().fromJson(json, type);
    }

    public static <T> T json2Obj(StringBuilder json, Class<T> type) {
        return json2Obj(json.toString(), type);
    }

    public static <T> String obj2Json(T t, Class<T> type) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(t, type);
        return json;
    }

    public static <T> String obj2Json(T t) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(t);
        return json;
    }
}

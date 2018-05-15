package com.tqhy.dcm4che.storescp.utils;

import com.google.gson.Gson;

/**
 * json解析工具类
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class JsonUtils {

    public static <T> T parseToMsg(String json, Class<T> msgType) {
        return new Gson().fromJson(json, msgType);
    }

    public static <T> T parseToMsg(StringBuilder json, Class<T> msgType) {
        return parseToMsg(json.toString(),msgType);
    }
}

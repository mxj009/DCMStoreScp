package com.tqhy.dcm4che.storescp.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tqhy.dcm4che.entity.InitScuData;
import com.tqhy.dcm4che.msg.BaseMsg;
import com.tqhy.dcm4che.msg.InitScuMsg;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 发送初始化客户端source,part,type消息任务
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class InitScuTask extends BaseTask {

    private InitScuMsg initScuMsg;

    public BaseMsg call() {
        //访问后台接口获取初始化来源,部位,类型信息
        System.out.println(getClass().getSimpleName() + " run() start...");
        BaseMsg connMsg = getInitDataByOkHttp();
        //BaseMsg connMsg = fakeGetInitData();
        if (BaseMsg.SUCCESS == connMsg.getStatus()) {
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            try {
                System.out.println("InitScuTask begin send init info..." + this.initScuMsg);
                oos = out;
                oos.writeObject(this.initScuMsg);
                oos.flush();
                System.out.println("InitScuTask send init info complete...");
                return new BaseMsg(BaseMsg.SUCCESS);
            } catch (EOFException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                releaseStream();
            }
        }

        return new BaseMsg(BaseMsg.FAILE);
    }

    public BaseMsg fakeGetInitData() {
        String msg = "{\"data\":{\"part\":[\"床旁\",\"ISIC\",\"曲面\",\"胸部\",\"主动脉\",\"口腔\",\"骨肌\"],\"source\":[\"主动脉窦\",\"胡总\",\"其它\",\"北医骨肌\",\"数据资料\",\"北大口腔\",\"安贞\",\"许玉峰老师\",\"双桥医院\",\"皮肤病\"],\"type\":[\"CT\",\"DR\"]},\"status\":\"1\",\"desc\":\"查询成功！\"}";
        System.out.println("SenScuInitMsgTask fakeGetInitData() in");
        initScuMsg = parseInitScuMsgFromJson(msg);
        System.out.println(initScuMsg);
        return new BaseMsg(BaseMsg.SUCCESS);
    }

    private InitScuMsg parseInitScuMsgFromJson(String msg) {
        System.out.println("InitScuTask parseInitScuMsgFromJson() start: " + msg);
        JSONObject json = JSONObject.fromObject(msg);
        int status = json.getInt("status");
        System.out.println("InitScuTask parseInitScuMsgFromJson() status: " + status);
        String desc = json.getString("desc");
        System.out.println("InitScuTask parseInitScuMsgFromJson() desc: " + desc);
        InitScuMsg initScuMsg = new InitScuMsg(status, desc);
        JSONObject dataJson = json.getJSONObject("data");
        InitScuData initScuData = new InitScuData();
        List<String> part = parseJsonArray(dataJson, "part");
        System.out.println("InitScuTask parseInitScuMsgFromJson() part: " + part);
        List<String> source = parseJsonArray(dataJson, "source");
        System.out.println("InitScuTask parseInitScuMsgFromJson() source: " + source);
        List<String> type = parseJsonArray(dataJson, "type");
        System.out.println("InitScuTask parseInitScuMsgFromJson() type: " + type);
        initScuData.setPart(part);
        initScuData.setSource(source);
        initScuData.setType(type);
        initScuMsg.setData(initScuData);
        return initScuMsg;
    }

    private List<String> parseJsonArray(JSONObject dataJson, String jsonName) {
        JSONArray partJson = dataJson.getJSONArray(jsonName);
        List<String> items = new ArrayList<>();
        for (int i = 0; i < partJson.size(); i++) {
            String item = (String) partJson.opt(i);
            items.add(item);
        }
        return items;
    }

    public BaseMsg getInitDataByHttpUrlConnection() {
        BufferedReader reader = null;
        try {
            URL url = new URL("http://192.168.1.243:8887/api/list");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("InitScuTask HttpURLConnection.HTTP_OK");
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = null;
                StringBuilder sb = new StringBuilder("");
                while ((line = reader.readLine()) != null) {
                    System.out.println("InitScuTask readLine: " + line);
                    sb.append(line);
                }

                System.out.println("InitScuTask read finish" + sb);
                //Gson gson = new GsonBuilder().serializeNulls().create();
                //this.initScuMsg = gson.fromJson(sb.toString(), InitScuMsg.class);
                this.initScuMsg = parseInitScuMsgFromJson(sb.toString());
                System.out.println("InitScuTask initScuMsg is: " + this.initScuMsg);
                return new BaseMsg(BaseMsg.SUCCESS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new BaseMsg(BaseMsg.FAILE);
    }

    public BaseMsg getInitDataByOkHttp() {

        System.out.println("InitScuTask getInitDataByOkHttp() start...");
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            System.out.println("InitScuTask getInitDataByOkHttp() okhttp init...");
            Request req = new Request.Builder().url("http://192.168.1.214:8887/api/list").build();

            Response resp = okHttpClient.newCall(req).execute();
            if (resp.isSuccessful()) {
                int code = resp.code();
                System.out.println("InitScuTask http resp code is: " + code);
                String message = resp.message();
                System.out.println("InitScuTask http rsp message is: " + message);
                if (200 == code) {
                    String body = resp.body().string();
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    this.initScuMsg = gson.fromJson(body, InitScuMsg.class);
                    System.out.println("InitScuTask initScuMsg is: " + initScuMsg);
                    return new BaseMsg(BaseMsg.SUCCESS);
                }
                return new BaseMsg(BaseMsg.FAILE);
            }
            return new BaseMsg(BaseMsg.FAILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BaseMsg(BaseMsg.FAILE);
    }

    public InitScuMsg parseInitScuMsgFromJson() {
        return initScuMsg;
    }

    public void setInitScuMsg(InitScuMsg initScuMsg) {
        this.initScuMsg = initScuMsg;
    }
}

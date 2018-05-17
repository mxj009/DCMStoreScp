package com.tqhy.dcm4che.storescp.tasks;

import com.google.gson.Gson;
import com.tqhy.dcm4che.msg.BaseMsg;
import com.tqhy.dcm4che.msg.InitScuMsg;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 发送初始化客户端source,part,type消息任务
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class SendScuInitMsgTask extends BaseTask {

    private InitScuMsg initScuMsg;

    public BaseMsg call() {
        //访问后台接口获取初始化来源,部位,类型信息
        //BaseMsg connMsg = getInitData();
        BaseMsg connMsg = fakeGetInitData();

        if (BaseMsg.SUCCESS == connMsg.getStatus()) {
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            try {
                System.out.println("SendScuInitMsgTask begin send init info..." + this.initScuMsg);
                oos = out;
                oos.writeObject(this.initScuMsg);
                oos.flush();
                System.out.println("SendScuInitMsgTask send init info complete...");
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

    private BaseMsg fakeGetInitData() {
        String msg = "{\"data\":{\"part\":[\"床旁\",\"ISIC\",\"曲面\",\"胸部\",\"主动脉\",\"口腔\",\"骨肌\"],\"source\":[\"主动脉窦\",\"胡总\",\"其它\",\"北医骨肌\",\"数据资料\",\"北大口腔\",\"安贞\",\"许玉峰老师\",\"双桥医院\",\"皮肤病\"],\"type\":[\"CT\",\"DR\"]},\"status\":\"1\",\"desc\":\"查询成功！\"}";
        this.initScuMsg = new Gson().fromJson(msg, InitScuMsg.class);
        System.out.println("SenScuInitMsgTask fakeGetInitData: " + initScuMsg);
        return new BaseMsg(BaseMsg.SUCCESS);
    }

    private BaseMsg getInitData() {

        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request req = new Request.Builder().url("http://192.168.1.219:8887/api/list").build();
            Response resp = okHttpClient.newCall(req).execute();
            if (resp.isSuccessful()) {
                int code = resp.code();
                System.out.println("http resp code is: " + code);
                String message = resp.message();
                System.out.println("http rsp message is: " + message);
                if (200 == code) {
                    String body = resp.body().string();
                    //System.out.println(body);
                    this.initScuMsg = new Gson().fromJson(body, InitScuMsg.class);
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

    public InitScuMsg getInitScuMsg() {
        return initScuMsg;
    }

    public void setInitScuMsg(InitScuMsg initScuMsg) {
        this.initScuMsg = initScuMsg;
    }
}

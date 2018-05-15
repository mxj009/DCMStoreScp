package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.msg.BaseMsg;
import com.tqhy.dcm4che.msg.InitScuMsg;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 *
 * 发送初始化客户端source,part,type消息任务
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class SendScuInitMsgTask extends BaseTask implements Callable<BaseMsg> {
    private Socket socket;
    private InitScuMsg initScuMsg;

    @Override
    public BaseMsg call() throws Exception {
        ObjectOutputStream oos = null;
        try {
            System.out.println("SendScuInitMsgTask begin send init info");
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(initScuMsg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public InitScuMsg getInitScuMsg() {
        return initScuMsg;
    }

    public void setInitScuMsg(InitScuMsg initScuMsg) {
        this.initScuMsg = initScuMsg;
    }
}

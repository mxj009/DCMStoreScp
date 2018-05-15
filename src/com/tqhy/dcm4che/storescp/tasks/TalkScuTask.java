package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.msg.ScuCommandMsg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * 和客户端会话任务
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class TalkScuTask implements Callable<ScuCommandMsg> {

    private Socket socket;

    @Override
    public ScuCommandMsg call() {
        ObjectInputStream ois = null;
        try {
            System.out.println("TalkScuTask start run...");
            ois = new ObjectInputStream(socket.getInputStream());
            ScuCommandMsg scuCommandMsg= (ScuCommandMsg) ois.readObject();
            int command = scuCommandMsg.getCommand();
            System.out.println("TalkScuTask ScuCommandMsg.getCommand()... " + command);
            return scuCommandMsg;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

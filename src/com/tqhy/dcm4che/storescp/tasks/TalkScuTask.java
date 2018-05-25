package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.msg.ScuCommandMsg;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 和客户端会话任务
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class TalkScuTask extends BaseTask {

    public ScuCommandMsg call() {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = in;
            oos = out;
            ScuCommandMsg scuCommandMsg = (ScuCommandMsg) ois.readObject();
            int command = scuCommandMsg.getCommand();
            System.out.println("TalkScuTask ScuCommandMsg.getCommand()... " + command);
            return scuCommandMsg;
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            releaseStream();
        }
        return new ScuCommandMsg(1);
    }
}

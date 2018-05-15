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
public class TalkScuTask implements Callable {

    private Socket socket;

    @Override
    public Callable call() {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            System.out.println("TalkScuTask start run...");
            ois = new ObjectInputStream(socket.getInputStream());
            ScuCommandMsg scuCommandMsg= (ScuCommandMsg) ois.readObject();
            int command = scuCommandMsg.getCommand();
            System.out.println("TalkScuTask ScuCommandMsg.getCommand()... " + command);
            switch (command) {
                case ScuCommandMsg.GET_ALL_INIT_INFO:
                    SendScuInitMsgTask sendScuInitMsgTask = new SendScuInitMsgTask();
                    sendScuInitMsgTask.setTaskType(BaseTask.INIT_MSG_TO_SCU_TASK);
                    sendScuInitMsgTask.setSocket(socket);
                    return sendScuInitMsgTask;
                case ScuCommandMsg.TRANSFER_ECXEL:
                    ExcelTask excelTask = new ExcelTask();
                    excelTask.setTaskType(BaseTask.EXCEL_TASK);
                    return excelTask;
            }
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

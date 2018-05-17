package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.AssembledBatch;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.entity.UploadCase;
import com.tqhy.dcm4che.msg.BaseMsg;
import com.tqhy.dcm4che.msg.ScuCommandMsg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * @author Yiheng
 * @create 2018/5/16
 * @since 1.0.0
 */
public class SCPTask implements Runnable {
    private Socket socket;
    private StoreScpTask storeScpTask;
    private AssembledBatch assembledBatch;
    private UploadCase uploadCase;


    @Override
    public void run() {
        try {
            TalkScuTask talkScuTask = new TalkScuTask();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            boolean flag = true;
            while (flag) {
                talkScuTask.setStream(in, out);
                ScuCommandMsg scuCommandMsg = talkScuTask.call();
                System.out.println("SCPTask operateTask TalkScuTask submit...");

                int command = scuCommandMsg.getCommand();
                System.out.println("SCPTask buildServerSocket command is: " + command);
                switch (command) {
                    case ScuCommandMsg.TRANSFER_DICOM_REQUEST:
                        storeScpTask.setStream(in, out);
                        storeScpTask.setUploadCase(uploadCase);
                        storeScpTask.setAssembledBatch(assembledBatch);
                        storeScpTask.call();
                        flag = false;
                        break;
                    case ScuCommandMsg.CREATE_BATCH_REQUEST:
                        CreateBatchTask createBatchTask = new CreateBatchTask();
                        createBatchTask.setStream(in, out);
                        assembledBatch = createBatchTask.call();
                        System.out.println("SCPTask ScuCommandMsg.CREATE_BATCH_REQUEST complete..." + assembledBatch);
                        break;
                    case ScuCommandMsg.TRANSFER_ECXEL_REQUEST:
                        ExcelTask excelTask = new ExcelTask();
                        excelTask.setStream(in, out);
                        excelTask.setSdConfig(storeScpTask.getSdConfig());
                        List<ImgCase> imgCasesFromExcel = excelTask.call();
                        uploadCase = new UploadCase();
                        uploadCase.setData(imgCasesFromExcel);
                        uploadCase.setBatch(assembledBatch.getBatch());

                        break;
                    case ScuCommandMsg.GET_ALL_INIT_INFO:
                        SendScuInitMsgTask sendScuInitMsgTask = new SendScuInitMsgTask();
                        sendScuInitMsgTask.setStream(in, out);
                        BaseMsg msg = sendScuInitMsgTask.call();
                        System.out.println("SCPTask BaseTask.INIT_MSG_TO_SCU_TASK is done...msg status: " + msg.getStatus());
                        break;
                    default:
                        System.out.println("SCPTask ScuCommandMsg 为空");
                        flag = false;
                        break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public SCPTask(Socket socket, StoreScpTask storeScpTask) {
        this.socket = socket;
        this.storeScpTask = storeScpTask;
    }

}

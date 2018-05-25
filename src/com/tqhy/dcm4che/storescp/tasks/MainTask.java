package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.AssembledBatch;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.entity.UploadCase;
import com.tqhy.dcm4che.msg.BaseMsg;
import com.tqhy.dcm4che.msg.ScuCommandMsg;
import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import com.tqhy.dcm4che.storescp.configs.TransferCapabilityConfig;
import org.dcm4che3.net.service.BasicCStoreSCP;

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
public class MainTask implements Runnable {
    private final Socket socket;
    private AssembledBatch assembledBatch;
    private UploadCase uploadCase;
    private ConnectConfig connConfig;
    private StorageConfig sdConfig;

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
                System.out.println("MainTask operateTask TalkScuTask submit...");

                int command = scuCommandMsg.getCommand();
                System.out.println("MainTask buildServerSocket command is: " + command);
                switch (command) {
                    case ScuCommandMsg.TRANSFER_DICOM_REQUEST:
                        StoreScpTask storeScpTask = new StoreScpTask();
                        storeScpTask.setConnectConfig(connConfig);
                        storeScpTask.setTcConfig(new TransferCapabilityConfig());
                        storeScpTask.setSdConfig(sdConfig);
                        storeScpTask.setStream(in, out);
                        storeScpTask.setUploadCase(uploadCase);
                        storeScpTask.setAssembledBatch(assembledBatch);
                        System.out.println(getClass().getSimpleName() + " TRANSFER_DICOM_REQUEST ..." + assembledBatch.getBatch());
                        storeScpTask.call();
                        flag = false;
                        break;
                    case ScuCommandMsg.CREATE_BATCH_REQUEST:
                        BatchTask batchTask = new BatchTask();
                        batchTask.setStream(in, out);
                        assembledBatch = batchTask.call();
                        System.out.println(getClass().getSimpleName() + " CREATE_BATCH_REQUEST ..." + assembledBatch.getBatch());
                        break;
                    case ScuCommandMsg.TRANSFER_ECXEL_REQUEST:
                        ExcelTask excelTask = new ExcelTask();
                        excelTask.setStream(in, out);
                        excelTask.setSdConfig(sdConfig, assembledBatch);
                        List<ImgCase> imgCasesFromExcel = excelTask.call();
                        uploadCase = new UploadCase();
                        uploadCase.setData(imgCasesFromExcel);
                        uploadCase.setBatch(assembledBatch.getBatch());
                        break;
                    case ScuCommandMsg.GET_ALL_INIT_INFO:
                        InitScuTask initScuTask = new InitScuTask();
                        initScuTask.setStream(in, out);
                        BaseMsg msg = initScuTask.call();
                        System.out.println("MainTask BaseTask.INIT_MSG_TO_SCU_TASK is done...msg status: " + msg.getStatus());
                        break;
                    default:
                        System.out.println("MainTask ScuCommandMsg 为空");
                        flag = false;
                        break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MainTask(Socket socket, ConnectConfig connConfig, StorageConfig sdConfig) {
        this.socket = socket;
        this.connConfig = connConfig;
        this.sdConfig = sdConfig;
    }

}

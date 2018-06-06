package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.AssembledBatch;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.entity.UploadCase;
import com.tqhy.dcm4che.msg.BaseMsg;
import com.tqhy.dcm4che.msg.ScuCommandMsg;
import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Yiheng
 * @create 2018/5/16
 * @since 1.0.0
 */
public class MainTask implements Runnable {
    private final Socket socket;
    private Device device;
    private AssembledBatch assembledBatch;
    private UploadCase uploadCase;
    private ConnectConfig connectConfig;
    private StorageConfig storageConfig;
    private Connection conn;

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
                        StoreScpTask storeScpTask = new StoreScpTask(connectConfig, storageConfig, uploadCase, assembledBatch);
                        storeScpTask.setStream(in, out);
                        System.out.println(getClass().getSimpleName() + " TRANSFER_DICOM_REQUEST ..." + assembledBatch.getBatch());
                        storeScpTask.call(device);
                        flag = false;
                        break;
                    case ScuCommandMsg.CREATE_BATCH_REQUEST:
                        BatchTask batchTask = new BatchTask();
                        batchTask.setStream(in, out);
                        assembledBatch = batchTask.call();
                        System.out.println(getClass().getSimpleName() + " CREATE_BATCH_REQUEST ..." + assembledBatch.getBatch());
                        initDeviceConnection();
                        break;
                    case ScuCommandMsg.TRANSFER_ECXEL_REQUEST:
                        ExcelTask excelTask = new ExcelTask();
                        excelTask.setStream(in, out);
                        excelTask.init(storageConfig, assembledBatch, ScuCommandMsg.TRANSFER_ECXEL_READY);
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
                    case ScuCommandMsg.TRANSFER_JPG_REQUEST:
                        JpgTask jpgTask = new JpgTask();
                        jpgTask.setStream(in, out);
                        jpgTask.setUploadCase(uploadCase);
                        jpgTask.init(storageConfig, assembledBatch, ScuCommandMsg.TRANSFER_JPG_READY);
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.submit(jpgTask);
                        flag = false;
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

    /**
     * 初始化Device配置
     */
    private void initDeviceConnection() {
        this.connectConfig = new ConnectConfig();
        connectConfig.init(assembledBatch.getAeAtHostPort());
        device = new Device(assembledBatch.getBatch().getBatchNo());
        ExecutorService pool = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        device.setScheduledExecutor(scheduledExecutorService);
        device.setExecutor(pool);

        conn = new Connection();
        conn.setHostname(connectConfig.getHost());
        int port = connectConfig.getPort();
        conn.setPort(port == 0 ? ConnectConfig.DEFAULT_PORT : port);
        device.addConnection(conn);
        connectConfig.setConn(conn);
        try {
            device.bindConnections();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        device.setDimseRQHandler(serviceRegistry);
    }

    public MainTask(Socket socket, StorageConfig storageConfig) {
        this.socket = socket;
        this.storageConfig = storageConfig;
    }

}

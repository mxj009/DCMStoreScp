package com.tqhy.dcm4che.storescp.tasks;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tqhy.dcm4che.entity.AssembledBatch;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.entity.UploadCase;
import com.tqhy.dcm4che.msg.ConnConfigMsg;
import com.tqhy.dcm4che.msg.ScuCommandMsg;
import com.tqhy.dcm4che.msg.UpLoadInfoMsg;
import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import com.tqhy.dcm4che.storescp.configs.TransferCapabilityConfig;
import com.tqhy.dcm4che.storescp.utils.MqClientUtils;
import com.tqhy.dcm4che.storescp.utils.StringUtils;
import okhttp3.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.*;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.*;

/**
 * 文件上传服务端,文件接收
 *
 * @author Yiheng
 * @create 2018/5/8
 * @since 1.0.0
 */
public class StoreScpTask extends BaseTask {

    private static final Logger LOG = LoggerFactory.getLogger(StoreScpTask.class);
    private static ResourceBundle rb = ResourceBundle.getBundle("messages");
    private Device device;
    private ApplicationEntity ae;
    private Connection conn;
    private File storageDir;

    /**
     * 返回给客户端状态码,默认0000H
     */
    private int status;

    /**
     * 连接配置对象
     */
    private ConnectConfig connectConfig;

    /**
     * 传输能力配置对象
     */
    private TransferCapabilityConfig tcConfig;

    /**
     * 存储路径及方式配置
     */
    private StorageConfig sdConfig;

    /**
     * Excel解析出来的UploadCase集合
     */
    private List<ImgCase> imgCasesFromExcel;

    /**
     * 客户端上传批次信息,包括批次,来源,部位,类型
     */
    private AssembledBatch assembledBatch;

    /**
     * 上传到样本库病例
     */
    private UploadCase uploadCase;

    /**
     * 本次上传dicom文件数量
     */
    private int dicomFileCount;

    /**
     * 已经处理完的dicom文件数量
     */
    private int parsedFileCount;

    private BasicCStoreSCP cstoreSCP;

    public StoreScpTask() {
        cstoreSCP = new BasicCStoreSCP(new String[]{"*"}) {
            protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException {
                rsp.setInt(2304, VR.US, new int[]{status});
                // System.out.println("StoreScpTask store() start..");
                if (storageDir != null) {
                    String cuid = rq.getString(2);
                    String iuid = rq.getString(4096);
                    String tsuid = pc.getTransferSyntax();

                    System.out.println("StoreScpTask store() batch..." + assembledBatch.getBatch());
                    System.out.println("StoreScpTask store() thread..." + Thread.currentThread().getName());
                    File storeDir = new File(storageDir, assembledBatch.getBatch().getBatchNo());
                    if (!storeDir.exists()) {
                        storeDir.mkdir();
                    }
                    File file = new File(storeDir, iuid);

                    try {
                        storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid), data, file);
                        //System.out.println("MyStore store complete..." + file.getAbsolutePath());
                    } catch (Exception var11) {
                        deleteFile(as, file);
                        throw new DicomServiceException(272, var11);
                    }
                }
            }
        };
        conn = new Connection();

        ae = new ApplicationEntity("*");
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);

        device = new Device("storescp");
        DicomServiceRegistry serviceRegistry = createServiceRegistry();
        device.setDimseRQHandler(serviceRegistry);
        device.addConnection(conn);
        device.addApplicationEntity(ae);
    }

    private void storeTo(Association as, Attributes fmi, PDVInputStream data, File file) throws IOException {
        file.getParentFile().mkdirs();
        DicomOutputStream out = new DicomOutputStream(file);

        try {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        } finally {
            parse(file);
            SafeClose.close(out);
        }
    }

    /**
     * 解析上传后的Dicom文件为UploadCase对象,并与Excel中解析出的UploadCase集合中UploadCase比对,如果PatientID相同,则组装为
     * 新的UploadCase对象.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private void parse(File file) {
        UploadCaseTask uploadCaseTask = new UploadCaseTask(file, uploadCase, assembledBatch);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<UploadCase> uploadCaseFuture = executor.submit(uploadCaseTask);
        try {
            uploadCase = uploadCaseFuture.get();
            System.out.println("StoreScpTask parse() uploadCase is: " + uploadCase);
            parsedFileCount++;
            if (parsedFileCount == dicomFileCount) {
                //已经处理完毕所有上传文件
                //uploadCasesByHttp(uploadCase);
                uploadCasesByMq(uploadCase);
                parsedFileCount = 0;
                device.unbindConnections();
                device.reconfigure(new Device());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟上传
     *
     * @param uploadCase
     */
    private void uploadCasesByMq(UploadCase uploadCase) {
        Type type = new TypeToken<UploadCase>() {
        }.getType();
        String json = new Gson().toJson(uploadCase, type);
        System.out.println(getClass().getSimpleName() + " uploadCasesByMq() json is: " + json);
        MqClientUtils.getMqClient().sendMessage(json);
    }

    /**
     * 上传本次解析完毕所有病例
     *
     * @param uploadCase
     */
    private void uploadCasesByHttp(UploadCase uploadCase) {
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String json = new Gson().toJson(uploadCase);
        System.out.println(getClass().getSimpleName() + " uploadCasesByHttp() " + json);
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url("http://192.168.1.220:8887/api/dicom")
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.println(getClass().getSimpleName() + " uploadCasesByHttp() " + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile(Association as, File file) {
        if (file.delete()) {
            LOG.info("{}: M-DELETE {}", as, file);
        } else {
            LOG.warn("{}: M-DELETE {} failed!", as, file);
        }
    }

    private DicomServiceRegistry createServiceRegistry() {
        System.out.println(getClass().getSimpleName() + " createServiceRegistry()...");
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(cstoreSCP);
        return serviceRegistry;
    }

    public void setStorageDirectory(File storageDir) {
        if (storageDir != null) {
            storageDir.mkdirs();
        }
        this.storageDir = storageDir;
    }

    public ConnConfigMsg call() {
        //System.out.println(getClass().getSimpleName() + " call() start...");
        //System.out.println(getClass().getSimpleName() + " aeTitle: " + connectConfig.getAeTitle() + " hostName: " + connectConfig.getHost() + " port: " + connectConfig.getPort());
        System.out.println(getClass().getSimpleName() + " call() Batch: " + assembledBatch.getBatch());
        System.out.println(getClass().getSimpleName() + " call() thread: " + Thread.currentThread().getName());
        configureConnect(conn, connectConfig);
        try {
            bindConnect(conn, ae, connectConfig);
            configureTransferCapability(ae, tcConfig);
            configureStorageDirectory(sdConfig);
            ExecutorService pool = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            device.setScheduledExecutor(scheduledExecutorService);
            device.setExecutor(pool);
            device.bindConnections();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ScuCommandMsg transDicomReadymsg = new ScuCommandMsg(1);
            transDicomReadymsg.setCommand(ScuCommandMsg.TRANSFER_DICOM_READY);
            out.writeObject(transDicomReadymsg);
            out.flush();
            System.out.println(getClass().getSimpleName() + " write ScuCommandMsg.TRANSFER_DICOM_READY...");
            UpLoadInfoMsg upLoadInfoMsg = (UpLoadInfoMsg) in.readObject();
            dicomFileCount = upLoadInfoMsg.getDicomFileCount();
            System.out.println(getClass().getSimpleName() + " read UpLoadInfoMsg dicomFileCount is ..." + dicomFileCount);
            parsedFileCount = 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 配置连接
     *
     * @param conn          连接对象
     * @param connectConfig 如果为null,则new一个新对象,所有配置将采用默认配置
     */
    private void configureConnect(Connection conn, ConnectConfig connectConfig) {
        if (null == connectConfig) {
            connectConfig = new ConnectConfig();
        }
        int maxPdulenRcv = connectConfig.getMaxPdulenRcv();
        conn.setReceivePDULength(maxPdulenRcv > 0 ? maxPdulenRcv : Connection.DEF_MAX_PDU_LENGTH);

        int maxPdulenSnd = connectConfig.getMaxPdulenSnd();
        conn.setSendPDULength(maxPdulenSnd > 0 ? maxPdulenSnd : Connection.DEF_MAX_PDU_LENGTH);

        int maxOpsInvoked = connectConfig.getMaxOpsInvoked();
        conn.setMaxOpsInvoked(connectConfig.isNotAsync() ? 1 : (maxOpsInvoked > 0 ? maxOpsInvoked : 0));

        int maxOpsPerformed = connectConfig.getMaxOpsPerformed();
        conn.setMaxOpsPerformed(connectConfig.isNotAsync() ? 1 : (maxOpsPerformed > 0 ? maxOpsPerformed : 0));

        conn.setPackPDV(!connectConfig.isNotPackPdv());

        int connectTimeout = connectConfig.getConnectTimeout();
        conn.setConnectTimeout(connectTimeout > 0 ? maxOpsPerformed : 0);

        int requestTimeout = connectConfig.getRequestTimeout();
        conn.setRequestTimeout(requestTimeout > 0 ? requestTimeout : 0);

        int acceptTimeout = connectConfig.getAcceptTimeout();
        conn.setAcceptTimeout(acceptTimeout > 0 ? acceptTimeout : 0);

        int releaseTimeout = connectConfig.getReleaseTimeout();
        conn.setReleaseTimeout(releaseTimeout > 0 ? releaseTimeout : 0);

        int responseTimeout = connectConfig.getResponseTimeout();
        conn.setResponseTimeout(responseTimeout > 0 ? responseTimeout : 0);

        int retrieveTimeout = connectConfig.getRetrieveTimeout();
        conn.setRetrieveTimeout(retrieveTimeout > 0 ? retrieveTimeout : 0);

        int idleTimeout = connectConfig.getIdleTimeout();
        conn.setIdleTimeout(idleTimeout > 0 ? idleTimeout : 0);

        int socketCloseDelay = connectConfig.getSocketCloseDelay();
        conn.setSocketCloseDelay(socketCloseDelay > 0 ? socketCloseDelay : Connection.DEF_SOCKETDELAY);

        int sendBufferSize = connectConfig.getSendBufferSize();
        conn.setSendBufferSize(sendBufferSize > 0 ? sendBufferSize : 0);

        int receiveBufferSize = connectConfig.getReceiveBufferSize();
        conn.setReceiveBufferSize(receiveBufferSize > 0 ? receiveBufferSize : 0);

        conn.setTcpNoDelay(!connectConfig.isNotTcpDelay());
    }

    private void bindConnect(Connection conn, ApplicationEntity ae, ConnectConfig connectConfig) throws Exception {
        if (null == connectConfig) {
            throw new Exception("connectConfig 为null");
        } else {
            //hostName可以为null
            conn.setHostname(connectConfig.getHost());

            int port = connectConfig.getPort();
            conn.setPort(port == 0 ? ConnectConfig.DEFAULT_PORT : port);

            String aeTitle = connectConfig.getAeTitle();
            ae.setAETitle(StringUtils.isNotEmpty(aeTitle) ? aeTitle : ConnectConfig.DEFAULT_AE_TITLE);
            System.out.println("StoreScpTask bindConnect() complete...");
        }
    }

    private void configureStorageDirectory(StorageConfig sdConfig) {
        if (null == sdConfig) {
            sdConfig = new StorageConfig();
        }
        if (!sdConfig.isIgnore()) {
            String directory = sdConfig.getDirectory();
            setStorageDirectory(new File(StringUtils.isNotEmpty(directory) ? directory : StorageConfig.DEFAULT_DIRECTORY));

            if (StringUtils.isNotEmpty(sdConfig.getFilePath())) {
                //setStorageFilePathFormat(sdConfig.getFilePath());
            }
        }

    }

    private void configureTransferCapability(ApplicationEntity ae, TransferCapabilityConfig tcConfig) throws IOException {
        if (null == tcConfig) {
            return;
        }

        if (tcConfig.isAcceptUnknown()) {
            ae.addTransferCapability(new TransferCapability((String) null, "*", TransferCapability.Role.SCP, new String[]{"*"}));
        } else {
            String sopClassesPath = tcConfig.getSopClassesPath();
            Properties p = CLIUtils.loadProperties(sopClassesPath == null ? TransferCapabilityConfig.DEFAULT_SOP_CLASS_PATH : sopClassesPath, null);
            Iterator var3 = p.stringPropertyNames().iterator();

            while (var3.hasNext()) {
                String cuid = (String) var3.next();
                String ts = p.getProperty(cuid);
                TransferCapability tc = new TransferCapability(null, CLIUtils.toUID(cuid), TransferCapability.Role.SCP, CLIUtils.toUIDs(ts));
                ae.addTransferCapability(tc);
            }
        }

    }

    public ConnectConfig getConnectConfig() {
        return connectConfig;
    }

    public void setConnectConfig(ConnectConfig connectConfig) {
        this.connectConfig = connectConfig;
    }

    public TransferCapabilityConfig getTcConfig() {
        return tcConfig;
    }

    public void setTcConfig(TransferCapabilityConfig tcConfig) {
        this.tcConfig = tcConfig;
    }

    public StorageConfig getSdConfig() {
        return sdConfig;
    }

    public void setSdConfig(StorageConfig sdConfig) {
        this.sdConfig = sdConfig;
    }

    public UploadCase getUploadCase() {
        return uploadCase;
    }

    public void setUploadCase(UploadCase uploadCase) {
        this.uploadCase = uploadCase;
    }

    public AssembledBatch getAssembledBatch() {
        return assembledBatch;
    }

    public void setAssembledBatch(AssembledBatch assembledBatch) {
        this.assembledBatch = assembledBatch;
    }

    public BasicCStoreSCP getCstoreSCP() {
        return cstoreSCP;
    }
}

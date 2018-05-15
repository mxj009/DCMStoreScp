package com.tqhy.dcm4che.storescp;

import com.google.gson.Gson;
import com.tqhy.dcm4che.msg.ScuCommandMsg;
import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import com.tqhy.dcm4che.storescp.configs.TransferCapabilityConfig;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.msg.BaseMsg;
import com.tqhy.dcm4che.msg.ConnConfigMsg;
import com.tqhy.dcm4che.msg.InitScuMsg;
import com.tqhy.dcm4che.storescp.tasks.*;
import com.tqhy.dcm4che.storescp.utils.StringUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
public class MyStoreScp implements Callable<ConnConfigMsg> {

    private static final Logger LOG = LoggerFactory.getLogger(MyStoreScp.class);
    private static ResourceBundle rb = ResourceBundle.getBundle("messages");
    private final Device device;
    private final ApplicationEntity ae;
    private final Connection conn;
    private File storageDir;
    private AttributesFormat filePathFormat;

    /**
     * 初始化客户端source,type,part的message
     */
    private InitScuMsg initInfoMsgToScu;

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

    private final BasicCStoreSCP cstoreSCP = new BasicCStoreSCP(new String[]{"*"}) {
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException {
            rsp.setInt(2304, VR.US, new int[]{status});
            System.out.println("MyStoreScp store() start..");
            if (storageDir != null) {
                String cuid = rq.getString(2);
                String iuid = rq.getString(4096);
                String tsuid = pc.getTransferSyntax();
                /*System.out.println("#############################################################");
                System.out.println("MyStore store rq.toString(): " + rq.toString());
                System.out.println("MyStore store rsp.toString(): " + rsp.toString());
                System.out.println("#############################################################");*/
                File file = new File(storageDir, iuid + ".part");

                try {
                    storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid), data, file);
                    System.out.println("MyStore store complete..." + file.getAbsolutePath());
                    //renameTo(as, file, new File(storageDir, filePathFormat == null ? iuid : filePathFormat.format(parse(file))));

                } catch (Exception var11) {
                    deleteFile(as, file);
                    throw new DicomServiceException(272, var11);
                }
            }
        }
    };
    private List<ImgCase> imgCasesFromExcel;
    private ExecutorService pool;


    public MyStoreScp() {
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
        LOG.info("{}: M-WRITE {}", as, file);
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

    private void renameTo(Association as, File from, File dest) throws IOException {
        LOG.info("{}: M-RENAME {} to {}", new Object[]{as, from, dest});
        if (!dest.getParentFile().mkdirs()) {
            dest.delete();
        }

        if (!from.renameTo(dest)) {
            throw new IOException("Failed to rename " + from + " to " + dest);
        }
    }

    /**
     * 解析上传后的Dicom文件为ImgCase对象,并与Excel中解析出的ImgCase集合中ImgCase比对,如果PatientID相同,则组装为
     * 新的ImgCase对象.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private ImgCase parse(File file) throws IOException {
        Dcm2ImgCaseTask dcm2ImgCaseTask = new Dcm2ImgCaseTask(file);
        Future<ImgCase> imgCaseFuture = pool.submit(dcm2ImgCaseTask);
        try {
            ImgCase imgCase = imgCaseFuture.get();
            Future<ImgCase> assembleFuture = pool.submit(new AssembleImgCaseTask(imgCase, imgCasesFromExcel));
            ImgCase assembledCase = assembleFuture.get();
            System.out.println("MyStoreScp parse() assembledCase is: "+assembledCase);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void deleteFile(Association as, File file) {
        if (file.delete()) {
            LOG.info("{}: M-DELETE {}", as, file);
        } else {
            LOG.warn("{}: M-DELETE {} failed!", as, file);
        }

    }

    private DicomServiceRegistry createServiceRegistry() {
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

    public void setStorageFilePathFormat(String pattern) {
        this.filePathFormat = new AttributesFormat(pattern);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public ConnConfigMsg call() throws Exception {
        System.out.println("MyStoreScp call() start...");
        if (null == connectConfig) {
            return new ConnConfigMsg(ConnConfigMsg.CONFIG_FAIL, ConnConfigMsg.CONFIG_IS_NULL);
        }
        System.out.println("aeTitle: " + connectConfig.getAeTitle() + " hostName: " + connectConfig.getHost() + " port: " + connectConfig.getPort());
        configureConnect(conn, connectConfig);
        bindConnect(conn, ae, connectConfig);
        //main.setStatus(CLIUtils.getIntOption(cl, "status", 0));
        configureTransferCapability(ae, tcConfig);
        configureStorageDirectory(sdConfig);

        //访问接口,获取来源,类型,部位数据
        //initInfoMsgToScu = connectSampleLib();
        //todo
        initInfoMsgToScu = new Gson().fromJson("{\"part\":[{\"createTime\":1522222434000,\"delFlag\":1,\"id\":\"2c28c4b79af8445db766515c323a5aff\",\"name\":\"骨肌\",\"updateTime\":1522222434000},{\"createTime\":1503968936000,\"delFlag\":1,\"id\":\"5b571a16d61b42f7a7f5e8b9076605f8\",\"name\":\"口腔\",\"updateTime\":1503968936000},{\"createTime\":1515566629000,\"delFlag\":1,\"id\":\"5bd8be85670d4cfba7f141e9fb050ec9\",\"name\":\"主动脉\",\"updateTime\":1515566629000},{\"createTime\":1499826936000,\"delFlag\":1,\"id\":\"867d657ec68b447ab57f3dff6c3cf576\",\"name\":\"胸部\",\"updateTime\":1499826936000},{\"createTime\":1514343220000,\"delFlag\":1,\"id\":\"9f19e6bc41d148739a2d7b2dda228030\",\"name\":\"曲面\",\"updateTime\":1514343220000},{\"createTime\":1523177588000,\"delFlag\":1,\"id\":\"cab76a4dcc3b4b75977e2b9f59ad9031\",\"name\":\"ISIC\",\"updateTime\":1523177588000},{\"createTime\":1505125602000,\"delFlag\":1,\"id\":\"f530443b1a1947799638b15805f35269\",\"name\":\"床旁\",\"updateTime\":1505125602000}],\"source\":[{\"createTime\":1523177723000,\"delFlag\":1,\"id\":\"3525de9456e54607b5ccf0aad18920ec\",\"name\":\"皮肤病\",\"updateTime\":1523177723000},{\"createTime\":1499824161000,\"delFlag\":1,\"id\":\"3f8b081edd0c4060805bf6a077f30679\",\"name\":\"双桥医院\",\"updateTime\":1499824161000},{\"createTime\":1512365180000,\"delFlag\":1,\"id\":\"43172961c8eb44a1854499576af10db5\",\"name\":\"许玉峰老师\",\"updateTime\":1512365180000},{\"createTime\":1505125718000,\"delFlag\":1,\"id\":\"66ddbafe669245f4b58c2c175f435e94\",\"name\":\"安贞\",\"updateTime\":1505125718000},{\"createTime\":1514342971000,\"delFlag\":1,\"id\":\"8310f5b97fe54a6495688263fa6ca928\",\"name\":\"北大口腔\",\"updateTime\":1514342971000},{\"createTime\":1515551362000,\"delFlag\":1,\"id\":\"87670ceda5ee42299258a9fdf5c361bd\",\"name\":\"数据资料\",\"updateTime\":1515551362000},{\"createTime\":1522378201000,\"delFlag\":1,\"id\":\"c1d0878591104f28b2ca2e6dc4cc5bb9\",\"name\":\"北医骨肌\",\"updateTime\":1522378201000},{\"createTime\":1503969044000,\"delFlag\":1,\"id\":\"dfbee262e15b46d8bc08d1532996bc15\",\"name\":\"其它\",\"updateTime\":1503969044000},{\"createTime\":1500520307000,\"delFlag\":1,\"id\":\"e36fbde330594cd6a8d9e5c66551d12d\",\"name\":\"胡总\",\"updateTime\":1500520307000},{\"createTime\":1515565829000,\"delFlag\":1,\"id\":\"f887334808594a478f65a10925e8e601\",\"name\":\"主动脉窦\",\"updateTime\":1515565829000}],\"type\":[{\"createTime\":1499826937000,\"delFlag\":1,\"id\":\"6c77cb00e3e743bc963ed71f1a2f5082\",\"name\":\"DR\",\"updateTime\":1499826937000},{\"createTime\":1508479424000,\"delFlag\":1,\"id\":\"de38edcc7ff1461ab8d2185ef6d66ad4\",\"name\":\"CT\",\"updateTime\":1508479424000}],\"status\":\"1\",\"desc\":\"查询成功！\"}", InitScuMsg.class);
        //System.out.println("MyStoreScp connectSampleLib " + initInfoMsgToScu.getPart());

        if (null != initInfoMsgToScu) {
            pool = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            device.setScheduledExecutor(scheduledExecutorService);
            device.setExecutor(pool);
            device.bindConnections();

            buildServerSocket();
            return new ConnConfigMsg(ConnConfigMsg.CONFIG_SUCCESS);
        }
        return new ConnConfigMsg(ConnConfigMsg.CONFIG_SUCCESS, BaseMsg.UNKNOWN_ERROR);
    }

    private InitScuMsg connectSampleLib() {

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
                    InitScuMsg initScuMsg = new Gson().fromJson(body, InitScuMsg.class);
                    return initScuMsg;
                }
                return null;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void buildServerSocket() {

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(connectConfig.getPort() + 1);
                Socket socket = null;
                TalkScuTask talkScuTask = null;
                ExecutorService pool = Executors.newCachedThreadPool();
                while (true) {
                    System.out.println("MyStoreScp buildServerSocket() serverSocket..." + serverSocket.getLocalPort());
                    socket = serverSocket.accept();
                    System.out.println("MyStoreScp buildServerSocket() accept socket: " + socket);
                    talkScuTask = new TalkScuTask();
                    talkScuTask.setSocket(socket);
                    operateTask(talkScuTask, pool);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void operateTask(TalkScuTask talkScuTask, ExecutorService pool) throws InterruptedException, ExecutionException {
        Future submit = pool.submit(talkScuTask);
        System.out.println("MyStoreScp operateTask TalkScuTask submit...");
        ScuCommandMsg scuCommandMsg = (ScuCommandMsg) submit.get();
        int command = scuCommandMsg.getCommand();
        System.out.println("MystoreScp buildServerSocket command is: " + command);
        switch (command) {
            case ScuCommandMsg.TRANSFER_ECXEL_REQUEST:
                ExcelTask excelTask = new ExcelTask();
                excelTask.setTaskType(BaseTask.EXCEL_TASK);
                excelTask.setSocket(talkScuTask.getSocket());
                excelTask.setSdConfig(sdConfig);
                Future<List<ImgCase>> imgListFuture = pool.submit(excelTask);
                //if (imgListFuture.isDone()) {
                imgCasesFromExcel = imgListFuture.get();
                System.out.println("MyStoreScp ExcelTask complete...imgCasesFromExcel.size is: " + imgCasesFromExcel.size());
                operateTask(talkScuTask, pool);
                //}
                break;
            case ScuCommandMsg.GET_ALL_INIT_INFO:
                SendScuInitMsgTask sendScuInitMsgTask = new SendScuInitMsgTask();
                sendScuInitMsgTask.setSocket(talkScuTask.getSocket());
                sendScuInitMsgTask.setInitScuMsg(initInfoMsgToScu);
                Future scuTaskFuture = pool.submit(sendScuInitMsgTask);
                //if (scuTaskFuture.isDone()) {
                BaseMsg msg = (BaseMsg) scuTaskFuture.get();
                System.out.println("BaseTask.INIT_MSG_TO_SCU_TASK is done...msg status: " + msg.getStatus());
                operateTask(talkScuTask, pool);
                //}
                System.out.println("BaseTask.INIT_MSG_TO_SCU_TASK is done???");
                break;
        }
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
            System.out.println("MyStoreScp bindConnect() complete...");
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
                setStorageFilePathFormat(sdConfig.getFilePath());
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
}

package com.tqhy.dcm4che.storescp;

import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import com.tqhy.dcm4che.storescp.configs.StorageDirectoryConfig;
import com.tqhy.dcm4che.storescp.configs.TransferCapabilityConfig;
import com.tqhy.dcm4che.storescp.utils.StringUtils;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
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
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 文件上传服务端,文件接收
 *
 * @author Yiheng
 * @create 2018/5/8
 * @since 1.0.0
 */
public class MyStoreScp implements Callable<String> {

    private static final Logger LOG = LoggerFactory.getLogger(MyStoreScp.class);
    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.storescp.messages");
    private final Device device;
    private final ApplicationEntity ae;
    private final Connection conn;
    private File storageDir;
    private AttributesFormat filePathFormat;

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
    private StorageDirectoryConfig sdConfig;

    private final BasicCStoreSCP cstoreSCP = new BasicCStoreSCP(new String[]{"*"}) {
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException {
            rsp.setInt(2304, VR.US, new int[]{status});
            if (storageDir != null) {
                String cuid = rq.getString(2);
                String iuid = rq.getString(4096);
                String tsuid = pc.getTransferSyntax();
                File file = new File(storageDir, iuid + ".part");

                try {
                    storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid), data, file);
                    renameTo(as, file, new File(storageDir, filePathFormat == null ? iuid : filePathFormat.format(parse(file))));
                } catch (Exception var11) {
                    deleteFile(as, file);
                    throw new DicomServiceException(272, var11);
                }
            }
        }
    };

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

    private Attributes parse(File file) throws IOException {
        DicomInputStream in = new DicomInputStream(file);

        Attributes var2;
        try {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            var2 = in.readDataset(-1, 2145386512);
        } finally {
            SafeClose.close(in);
        }

        return var2;
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
        serviceRegistry.addDicomService(this.cstoreSCP);
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
    public String call() throws Exception {
        if (null == connectConfig) {
            return "连接配置为空";
        }
        configureConnect(conn, connectConfig);
        bindConnect(conn, ae, connectConfig);
        //main.setStatus(CLIUtils.getIntOption(cl, "status", 0));
        configureTransferCapability(ae, tcConfig);
        configureStorageDirectory(sdConfig);

        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        device.setScheduledExecutor(scheduledExecutorService);
        device.setExecutor(executorService);
        device.bindConnections();
        return "服务已启动";
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

            String port = connectConfig.getPort();
            conn.setPort(StringUtils.isNotEmpty(port) ? Integer.parseInt(port) : ConnectConfig.DEFAULT_PORT);

            String aeTitle = connectConfig.getAeTitle();
            ae.setAETitle(StringUtils.isNotEmpty(aeTitle) ? aeTitle : ConnectConfig.DEFAULT_AE_TITLE);
        }
    }

    private void configureStorageDirectory(StorageDirectoryConfig sdConfig) {
        if (!sdConfig.isIgnore()) {
            String directory = sdConfig.getDirectory();
            setStorageDirectory(new File(StringUtils.isNotEmpty(directory) ? directory : StorageDirectoryConfig.DEFAULT_DIRECTORY));

            if (StringUtils.isNotEmpty(sdConfig.getFilePath())) {
                setStorageFilePathFormat(sdConfig.getFilePath());
            }
        }

    }

    private static void configureTransferCapability(ApplicationEntity ae, TransferCapabilityConfig tcConfig) throws IOException {
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

    public StorageDirectoryConfig getSdConfig() {
        return sdConfig;
    }

    public void setSdConfig(StorageDirectoryConfig sdConfig) {
        this.sdConfig = sdConfig;
    }
}
package com.tqhy.dcm4che.storescp.tasks;

import com.google.gson.Gson;
import com.tqhy.dcm4che.entity.AssembledBatch;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.entity.UploadCase;
import com.tqhy.dcm4che.msg.ConnConfigMsg;
import com.tqhy.dcm4che.msg.ScuCommandMsg;
import com.tqhy.dcm4che.msg.UpLoadInfoMsg;
import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import com.tqhy.dcm4che.storescp.configs.TransferCapabilityConfig;
import com.tqhy.dcm4che.storescp.utils.JsonUtils;
import com.tqhy.dcm4che.storescp.utils.ActiveMqClientUtils;
import com.tqhy.dcm4che.storescp.utils.RabbitMqClientUtils;
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * 文件上传服务端,文件接收
 *
 * @author Yiheng
 * @create 2018/5/8
 * @since 1.0.0
 */
public class StoreScpTask extends BaseTask {

    /**
     * 除Connection与title外,均采用默认配置
     */
    private ApplicationEntity ae;

    /**
     * 文件存储根路径,由{@link StorageConfig}完成初始化
     */
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
    private StorageConfig storageConfig;

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

    /**
     * 具体负责存储与解析的CStoreSCP对象
     */
    private BasicCStoreSCP cstoreSCP;

    public StoreScpTask(ConnectConfig connectConfig, StorageConfig storageConfig, UploadCase uploadCase, AssembledBatch assembledBatch) {
        setConnectConfig(connectConfig);
        setStorageConfig(storageConfig);
        setUploadCase(uploadCase);
        setAssembledBatch(assembledBatch);
        setAe(connectConfig);
        setTcConfig(new TransferCapabilityConfig());
        cstoreSCP = new BasicCStoreSCP(new String[]{"*"}) {
            protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException {
                rsp.setInt(2304, VR.US, new int[]{status});
                // System.out.println("StoreScpTask store() start..");
                if (storageDir != null) {
                    String cuid = rq.getString(2);
                    String iuid = rq.getString(4096);
                    String tsuid = pc.getTransferSyntax();

                    System.out.println(this + " StoreScpTask store() batch..." + assembledBatch.getBatch());
                    System.out.println(this + " StoreScpTask store() thread..." + Thread.currentThread().getName());
                    File storeDir = new File(storageDir, assembledBatch.getBatch().getBatchNo());
                    if (!storeDir.exists()) {
                        storeDir.mkdir();
                    }
                    File file = new File(storeDir, iuid + ".part");

                    try {
                        storeTo(as.createFileMetaInformation(iuid, cuid, tsuid), data, file);
                        renameTo(file, new File(storeDir, iuid));
                        //System.out.println("MyStore store complete..." + file.getAbsolutePath());
                    } catch (Exception var11) {
                        deleteFile(file);
                        throw new DicomServiceException(272, var11);
                    }
                }
            }
        };
    }

    /**
     * 重命名文件,确保文件上传完成,上传完成后调用{@link #parse(File)}方法对
     * 上传文件进行解析.
     *
     * @param from 待重命名文件
     * @param dest 重命名后文件
     * @see #parse(File)
     */
    private void renameTo(File from, File dest) {
        if (!dest.getParentFile().mkdirs()) {
            dest.delete();
        }
        try {
            if (!from.renameTo(dest)) {
                throw new IOException("Failed to rename " + from + " to " + dest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            parse(dest);
        }
    }

    /**
     * 保存上传文件,该方法中保存的文件并非最终使用的DCM文件,而是<i>.part</i>结尾的临时文件;
     * 调用该方法后,必须再调用{@link #renameTo(File, File)}方法,以确保文件上传完成,
     * 在进行其他操作,比如调用{@link #parse(File)}进行DCM文件解析的操作.
     *
     * @param fmi  文件元信息
     * @param data PDV流数据
     * @param file 接收上传的临时文件
     * @throws IOException
     * @see #renameTo(File, File)
     */
    private void storeTo(Attributes fmi, PDVInputStream data, File file) throws IOException {
        file.getParentFile().mkdirs();
        DicomOutputStream out = new DicomOutputStream(file);
        try {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        } finally {
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
        uploadCase = uploadCaseTask.call();
        System.out.println(this.cstoreSCP + " StoreScpTask parse() uploadCase is: " + uploadCase);
        parsedFileCount++;
        if (parsedFileCount == dicomFileCount) {
            //已经处理完毕所有上传文件
            //uploadCasesByHttp(uploadCase);
            uploadCasesByMq(uploadCase);
            parsedFileCount = 0;

        }
    }

    /**
     * 模拟上传
     *
     * @param uploadCase
     */
    private void uploadCasesByMq(UploadCase uploadCase) {
        String json = JsonUtils.obj2Json(uploadCase, UploadCase.class);
        System.out.println(getClass().getSimpleName() + " uploadCasesByMq() json is: " + json);
        //ActiveMqClientUtils.getMqClient().sendMessage(json, "img.dicom.queue");
        RabbitMqClientUtils.getMqClient().sendMessage(json,"img.dicom.queue");
        System.out.println("send msg finish...");
        releaseResources();
    }

    private void releaseResources() {
        Device device = ae.getDevice();
        device.unbindConnections();
        List<Connection> conns = ae.getConnections();
        Iterator<Connection> iterator = conns.iterator();
        while (iterator.hasNext()) {
            Connection conn = iterator.next();
            conn.unbind();
            //ae.removeConnection(conn);
        }
        device = null;
        ae = null;
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

    /**
     * 文件上传失败则删除临时文件
     *
     * @param file 要删除的临时文件
     * @see #parse(File)
     */
    private void deleteFile(File file) {
        if (file.delete()) {
            System.out.println(getClass().getSimpleName() + "delete file: " + file);
        } else {
            System.gc();
            System.out.println(getClass().getSimpleName() + "delete file " + file + " : " + file.delete());
        }
    }

    /**
     * 向Device对象注册服务
     *
     * @return
     */
    private DicomServiceRegistry createServiceRegistry() {
        System.out.println(getClass().getSimpleName() + " createServiceRegistry()...");
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(cstoreSCP);
        return serviceRegistry;
    }

    /**
     * 设置存储根路径
     *
     * @param storageDir 存储根路径
     */
    public void setStorageDirectory(File storageDir) {
        if (storageDir != null) {
            storageDir.mkdirs();
        }
        this.storageDir = storageDir;
    }

    public ConnConfigMsg call(Device device) {
        //System.out.println(getClass().getSimpleName() + " run() start...");
        //System.out.println(getClass().getSimpleName() + " aeTitle: " + connectConfig.getAeTitle() + " hostName: " + connectConfig.getHost() + " port: " + connectConfig.getPort());
        System.out.println(getClass().getSimpleName() + " run() Batch: " + assembledBatch.getBatch());
        System.out.println(getClass().getSimpleName() + " run() thread: " + Thread.currentThread().getName());
        try {
            configureTransferCapability();
            configureStorageDirectory();
            configureDevice(device);
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

    private void configureDevice(Device device) {
        device.addApplicationEntity(ae);
        ((DicomServiceRegistry) device.getDimseRQHandler()).addDicomService(cstoreSCP);
    }

    /**
     * 给ApplicationEntity绑定链接
     *
     * @param conn
     * @param connectConfig
     * @throws Exception
     */
    private void bindConnect(Connection conn, ConnectConfig connectConfig) throws Exception {
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

    private void configureStorageDirectory() {
        if (null == storageConfig) {
            storageConfig = new StorageConfig();
        }
        if (!storageConfig.isIgnore()) {
            String directory = storageConfig.getDirectory();
            setStorageDirectory(new File(StringUtils.isNotEmpty(directory) ? directory : StorageConfig.DEFAULT_DIRECTORY));

            if (StringUtils.isNotEmpty(storageConfig.getFilePath())) {
                //setStorageFilePathFormat(storageConfig.getFilePath());
            }
        }

    }

    private void configureTransferCapability() throws IOException {
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

    public TransferCapabilityConfig getTcConfig() {
        return tcConfig;
    }

    public void setTcConfig(TransferCapabilityConfig tcConfig) {
        this.tcConfig = tcConfig;
    }

    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
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

    public ConnectConfig getConnectConfig() {
        return connectConfig;
    }

    public void setConnectConfig(ConnectConfig connectConfig) {
        this.connectConfig = connectConfig;
    }

    public ApplicationEntity getAe() {
        return ae;
    }

    public void setAe(ConnectConfig connectConfig) {
        ae = new ApplicationEntity("*");
        String aeTitle = connectConfig.getAeTitle();
        ae.setAETitle(StringUtils.isNotEmpty(aeTitle) ? aeTitle : ConnectConfig.DEFAULT_AE_TITLE);
        ae.setAssociationAcceptor(true);
        ae.addConnection(connectConfig.getConn());
    }
}

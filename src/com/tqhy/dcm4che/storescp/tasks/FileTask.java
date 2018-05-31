package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.AssembledBatch;
import com.tqhy.dcm4che.entity.Batch;
import com.tqhy.dcm4che.msg.ScuCommandMsg;
import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import com.tqhy.dcm4che.storescp.utils.StringUtils;

import java.io.*;

/**
 * 接收上传文件Task类的基类,子类包括ExcelTask,JpgTask
 *
 * @author Yiheng
 * @create 2018/5/30
 * @since 1.0.0
 */
public class FileTask extends BaseTask {

    protected int scuCommand;
    protected File storeDir;
    protected Batch batch;
    protected String target;

    /**
     * 保存上传文件到StorageConfig与Batch决定的路径下
     *
     * @return 返回保存的文件, 保存失败则返回null
     */
    protected File saveFile() {
        try {
            ScuCommandMsg transReadyMsg = new ScuCommandMsg(1);
            transReadyMsg.setCommand(scuCommand);
            out.writeObject(transReadyMsg);
            out.flush();

            System.out.println(getClass().getSimpleName() + " run...");

            //获取文件名
            String fname = (String) in.readObject();
            System.out.println(getClass().getSimpleName() + " file is: " + fname);

            //获取文件长度
            long fileLength = in.readLong();
            //获取解析后数据发往Test还是样本库
            target = (String) in.readObject();

            File file = new File(storeDir, fname.trim());
            System.out.println(getClass().getSimpleName() + " will save file to dir: " + file.getPath());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            byte[] bytes = new byte[1024 * 8];
            int len = 0;
            while ((len = in.read(bytes)) != -1 && file.length() < fileLength) {
                System.out.println(getClass().getSimpleName() + " writing file..." + len);
                System.out.println();
                bos.write(bytes, 0, len);
                bos.flush();
                if (file.length() == fileLength) {
                    System.out.println(getClass().getSimpleName() + "upload file complete...");
                    bos.close();
                    return file;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            releaseStream();
        }
        return null;
    }

    /**
     * 初始化文件保存任务所需参数,生成新的子类对象后必须调用该方法进行参数配置
     *
     * @param sdConfig       获取保存文件根路径,即应用启动界面设置的路径
     * @param assembledBatch 获取批次信息,批次号用于创建保存本批次上传文件的文件夹
     * @param scuCommand     返回给客户端的可以开始上传文件的ScuCommandMsg指令
     */
    public void init(StorageConfig sdConfig, AssembledBatch assembledBatch, int scuCommand) {
        this.scuCommand = scuCommand;
        String directory = sdConfig.getDirectory();
        this.batch = assembledBatch.getBatch();
        String batchNo = batch.getBatchNo();
        if (StringUtils.isNotEmpty(directory) && StringUtils.isNotEmpty(batchNo)) {
            File storeDir = new File(directory, batchNo);
            if (!storeDir.exists()) {
                storeDir.mkdir();
            }
            this.storeDir = storeDir;
        }
    }

    public String getTarget() {
        return target;
    }
}

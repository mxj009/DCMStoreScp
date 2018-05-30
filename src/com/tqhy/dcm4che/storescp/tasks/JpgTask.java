package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.UploadJpg;
import com.tqhy.dcm4che.storescp.utils.FileUtils;
import com.tqhy.dcm4che.storescp.utils.JsonUtils;

import java.io.File;
import java.util.List;

/**
 * 任务类,接收上传jpg文件,解析后上传消息队列
 * @author Yiheng
 * @create 2018/5/30
 * @since 1.0.0
 */
public class JpgTask extends FileTask implements Runnable {

    @Override
    public void run() {
        File savedFile = saveFile();
        List<String> jpgPaths = FileUtils.extractZipFiles(savedFile, null);
        UploadJpg uploadJpg = new UploadJpg(batch, jpgPaths);
        String json = JsonUtils.obj2Json(uploadJpg);
        System.out.println("JpgTask send json: " + json);
        //MqClientUtils.getMqClient().sendMessage(json,"queueName" );
    }
}

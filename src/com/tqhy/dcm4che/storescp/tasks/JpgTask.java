package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.entity.ImgCenter;
import com.tqhy.dcm4che.entity.UploadCase;
import com.tqhy.dcm4che.entity.UploadJpg;
import com.tqhy.dcm4che.storescp.utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务类,接收上传jpg文件,解析后上传消息队列
 *
 * @author Yiheng
 * @create 2018/5/30
 * @since 1.0.0
 */
public class JpgTask extends FileTask implements Runnable {

    private UploadCase uploadCase;

    @Override
    public void run() {
        File savedFile = saveFile();
        try {
            if (null == savedFile) {
                throw new Exception("保存Jpg异常");
            }
            List<String> jpgPaths = FileUtils.extractZipFiles(savedFile, null);
            String json = null;
            if ("TEST".equals(target)) {
                UploadJpg uploadJpg = new UploadJpg(batch, jpgPaths);
                json = JsonUtils.obj2Json(uploadJpg);
            } else if ("SAMPLE".equals(target)) {
                //装配UploadCase
                List<ImgCase> originImgCases = uploadCase.getData();
                System.out.println("originImgCases.size()..1" + originImgCases.size());
                List<ImgCase> assembledImgCases = new ArrayList<>();
                for (String path : jpgPaths) {
                    String patientId = FileUtils.getFileNameWithoutSuffix(path);
                    assembleImgCase(patientId, path, originImgCases, assembledImgCases);
                }
                System.out.println("originImgCases.size()..2" + originImgCases.size());
                if (originImgCases.size() > 0) {
                    assembledImgCases.addAll(originImgCases);
                }
                uploadCase.setData(assembledImgCases);
                json = JsonUtils.obj2Json(uploadCase);
            }
            System.out.println("JpgTask send json: " + json);
            MqClientUtils.getMqClient().sendMessage(json, "TEST".equals(target) ? "img.test.queue" : "img.dicom.queue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!savedFile.delete()) {
                System.gc();
                savedFile.delete();
                //System.out.println("zip file deleted: " + delete);
            }
        }
    }

    private void assembleImgCase(String patientId, String path, List<ImgCase> originImgCases, List<ImgCase> assembledImgCases) {
        File jpgFile = new File(path);
        int count = 0;
        for (ImgCase aCase : originImgCases) {
            count++;
            List<ImgCenter> imgCenters = makeImgCenters(path, jpgFile);
            if (StringUtils.equals(aCase.getPatientId(), patientId)) {
                aCase.setImgCenters(imgCenters);
                assembledImgCases.add(aCase);
                originImgCases.remove(aCase);
                break;
            }
            //如果遍历完仍未匹配成功,则添加两个ImgCase,一个来自Excel,一个来自jpg图片
            if (count == originImgCases.size()) {
                ImgCase newCase = new ImgCase();
                newCase.setImgCenters(imgCenters);
                assembledImgCases.add(newCase);
                break;
            }
        }
    }

    private List<ImgCenter> makeImgCenters(String path, File jpgFile) {
        ImgCenter imgCenter = new ImgCenter();
        List<ImgCenter> imgCenters = new ArrayList<>();
        imgCenter.setBatchNo(batch.getBatchNo());
        imgCenter.setImgMd5(FileUtils.getMD5(jpgFile));
        imgCenter.setImgUrl(path);
        imgCenter.setImg1024Url(ImgCenterUtils.getImg1024Url(jpgFile));
        imgCenter.setImgUrlThumb(ImgCenterUtils.getImgUrlThumb(jpgFile));
        imgCenters.add(imgCenter);
        return imgCenters;
    }

    public void setUploadCase(UploadCase uploadCase) {
        this.uploadCase = uploadCase;
    }
}

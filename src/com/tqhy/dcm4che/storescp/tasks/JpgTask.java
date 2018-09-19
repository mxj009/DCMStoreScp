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
                if (0 == originImgCases.size()) {
                    for (String path : jpgPaths) {
                        assembleImgCaseFromNew(originImgCases, path);
                    }
                } else {
                    List<ImgCase> assembledImgCases = new ArrayList<>();
                    for (String path : jpgPaths) {
                        String patientId = FileUtils.getFileNameWithoutSuffix(path);
                        assembleImgCaseFromOrigin(patientId, path, originImgCases, assembledImgCases);
                    }
                    System.out.println("originImgCases.size()..2" + originImgCases.size());
                    if (originImgCases.size() > 0) {
                        assembledImgCases.addAll(originImgCases);
                    }
                    uploadCase.setData(assembledImgCases);
                }
                json = JsonUtils.obj2Json(uploadCase);
            }
            System.out.println("JpgTask send json: " + json);
            ActiveMqClientUtils.getMqClient().sendMessage(json, "TEST".equals(target) ? "img.test.queue" : "img.dicom.queue");
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

    /**
     * 对应未上传Excel文件情况,originImgCases集合中病例数为0,则全部由jpg文件生成病例
     * 对象,然后放入originImgCases.
     *
     * @param originImgCases
     * @param path
     */
    private void assembleImgCaseFromNew(List<ImgCase> originImgCases, String path) {
        ImgCase imgCase = new ImgCase();
        imgCase.setBatchNo(batch.getBatchNo());
        String patientId = FileUtils.getFileNameWithoutSuffix(path);
        imgCase.setPatientId(patientId);
        File jpgFile = new File(path);
        List<ImgCenter> imgCenters = makeImgCenters(path, jpgFile);
        imgCase.setImgCenters(imgCenters);
        originImgCases.add(imgCase);
    }

    /**
     * 对应有上传Excel文件情况,此时originImgCases不为空,其中病例对象来自对Excel文件
     * 内容的解析,在该方法中需要将这些病例对象与Jpg图片patientId比对,比对成功则基于Jpg图片
     * 创建ImgCenter对象,并添加到ImgCase对象的imgCenters集合中,最后将该ImgCase对象置入
     * assembledImgCase集合中;如果遍历完仍未匹配成功,则添加两个ImgCase,一个来自Excel,一
     * 个来自jpg图片.
     *
     * @param patientId
     * @param path
     * @param originImgCases
     * @param assembledImgCases
     */
    private void assembleImgCaseFromOrigin(String patientId, String path, List<ImgCase> originImgCases, List<ImgCase> assembledImgCases) {
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

    /**
     * 创建ImgCenters对象
     *
     * @param path    jpg文件路径
     * @param jpgFile jpg文件对象
     * @return ImgCenter对象集合
     */
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
        if (null == uploadCase) {
            uploadCase = new UploadCase();
            uploadCase.setData(new ArrayList<ImgCase>());
        }
        this.uploadCase = uploadCase;
    }
}

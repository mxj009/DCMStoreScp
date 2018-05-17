package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.AssembledBatch;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.entity.UploadCase;

import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * 将Dicom解析出来的ImgCase对象与Excel解析出来ImgCase对象集合比对PatientId,如果相同则组装.
 * 无论Excel中是否有匹配上的对象,都会返回Dicom解析出来的ImgCase对象.
 *
 * @author Yiheng
 * @create 2018/5/15
 * @since 1.0.0
 */
public class AssembleUploadCaseTask extends BaseTask implements Callable<ImgCase> {
    private ImgCase imgCaseFromDcm;
    private List<ImgCase> imgCasesFromExcel;
    private AssembledBatch assembledBatch;

    @Override
    public ImgCase call() throws Exception {

        for (ImgCase aCase : imgCasesFromExcel) {
            if (aCase.getPatientId().equals(imgCaseFromDcm.getPatientId())) {
                imgCaseFromDcm.setImgInfo(aCase.getImgInfo());
                imgCaseFromDcm.setImgResult(aCase.getImgResult());
                imgCaseFromDcm.setAge(aCase.getAge());
                imgCaseFromDcm.setSource(assembledBatch.getSource());
                imgCaseFromDcm.setType(assembledBatch.getType());
                imgCaseFromDcm.setPart(assembledBatch.getPart());
            }
        }
        return imgCaseFromDcm;
    }

    public AssembleUploadCaseTask() {
    }

    public AssembleUploadCaseTask(UploadCase uploadCase, List<ImgCase> imgCasesFromExcel, AssembledBatch assembledBatch) {
        this.imgCaseFromDcm = imgCaseFromDcm;
        this.imgCasesFromExcel = imgCasesFromExcel;
        this.assembledBatch = assembledBatch;
    }

    public ImgCase getImgCaseFromDcm() {
        return imgCaseFromDcm;
    }

    public void setImgCaseFromDcm(ImgCase imgCaseFromDcm) {
        this.imgCaseFromDcm = imgCaseFromDcm;
    }

    public List<ImgCase> getImgCasesFromExcel() {
        return imgCasesFromExcel;
    }

    public void setImgCasesFromExcel(List<ImgCase> imgCasesFromExcel) {
        this.imgCasesFromExcel = imgCasesFromExcel;
    }

    public AssembledBatch getAssembledBatch() {
        return assembledBatch;
    }

    public void setAssembledBatch(AssembledBatch assembledBatch) {
        this.assembledBatch = assembledBatch;
    }
}

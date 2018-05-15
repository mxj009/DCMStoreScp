package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.ImgCase;

import java.util.ArrayList;
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
public class AssembleImgCaseTask extends BaseTask implements Callable<ImgCase> {
    private ImgCase imgCaseFromDcm;
    private List<ImgCase> imgCasesFromExcel;

    @Override
    public ImgCase call() throws Exception {

        for (ImgCase aCase : imgCasesFromExcel) {
            if (aCase.getPatientId().equals(imgCaseFromDcm.getPatientId())) {
                imgCaseFromDcm.setImgInfo(aCase.getImgInfo());
                imgCaseFromDcm.setImgResult(aCase.getImgResult());
                imgCaseFromDcm.setAge(aCase.getAge());
            }
        }
        return imgCaseFromDcm;
    }

    public AssembleImgCaseTask(ImgCase imgCaseFromDcm, List<ImgCase> imgCasesFromExcel) {
        this.imgCaseFromDcm = imgCaseFromDcm;
        this.imgCasesFromExcel = imgCasesFromExcel;
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

    public void setImgCasesFromExcel(ArrayList<ImgCase> imgCasesFromExcel) {
        this.imgCasesFromExcel = imgCasesFromExcel;
    }
}

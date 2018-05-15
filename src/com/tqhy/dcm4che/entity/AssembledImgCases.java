package com.tqhy.dcm4che.entity;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 发送给样本库后台数据实体类
 * @author Yiheng
 * @create 2018/5/15
 * @since 1.0.0
 */
public class AssembledImgCases implements Serializable{
    private static final long serialVersionUID = 1L;

    private List<ImgCase> imgCases;
    private Batch batch;

    public AssembledImgCases() {
    }

    public AssembledImgCases(List<ImgCase> imgCases, Batch batch) {
        this.imgCases = imgCases;
        this.batch = batch;
    }

    public List<ImgCase> getImgCases() {
        return imgCases;
    }

    public void setImgCases(List<ImgCase> imgCases) {
        this.imgCases = imgCases;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }
}

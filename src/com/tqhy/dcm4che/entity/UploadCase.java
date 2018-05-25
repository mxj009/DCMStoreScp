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
public class UploadCase implements Serializable{
    private static final long serialVersionUID = 1L;

    private List<ImgCase> data;
    private Batch batch;

    public UploadCase() {
    }

    public UploadCase(List<ImgCase> data, Batch batch) {
        this.data = data;
        this.batch = batch;
    }

    public List<ImgCase> getData() {
        return data;
    }

    public void setData(List<ImgCase> data) {
        this.data = data;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }
}

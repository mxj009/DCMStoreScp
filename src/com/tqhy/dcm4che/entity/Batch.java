package com.tqhy.dcm4che.entity;

import java.io.Serializable;

/**
 * 上传批次实体类
 *
 * @author Yiheng
 * @create 2018/5/15
 * @since 1.0.0
 */
public class Batch implements Serializable{
    private static final long serialVersionUID =1L;
    private String batchNo;
    private String desc;

    public Batch() {
    }

    public Batch(String batchNo, String desc) {
        this.batchNo = batchNo;
        this.desc = desc;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "Batch{" +
                "batchNo='" + batchNo + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}

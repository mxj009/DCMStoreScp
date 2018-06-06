package com.tqhy.dcm4che.entity;

import java.io.Serializable;

/**
 * 上传文件前上传批次信息实体类,包括一个Batch对象,一条Part信息,一条Source信息以及一条Type信息
 *
 * @author Yiheng
 * @create 2018/5/16
 * @since 1.0.0
 */
public class AssembledBatch implements Serializable {
    private static final long serialVersionUID = 1L;
    private String aeAtHostPort;
    private Batch batch;
    private String type;
    private String part;
    private String source;

    public AssembledBatch() {
    }

    public AssembledBatch(Batch batch, String type, String part, String source) {
        this.batch = batch;
        this.type = type;
        this.part = part;
        this.source = source;
    }

    public String getAeAtHostPort() {
        return aeAtHostPort;
    }

    public void setAeAtHostPort(String aeAtHostPort) {
        this.aeAtHostPort = aeAtHostPort;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}

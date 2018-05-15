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
    private String id;
    private String desc;

    public Batch() {
    }

    public Batch(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

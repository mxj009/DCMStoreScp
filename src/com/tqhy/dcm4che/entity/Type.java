package com.tqhy.dcm4che.entity;

import java.io.Serializable;

/**
 * 影像类型实体类
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class Type implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String createTime;
    private String updateTime;
    private int delFlag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(int delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "Type{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", delFlag=" + delFlag +
                '}';
    }
}

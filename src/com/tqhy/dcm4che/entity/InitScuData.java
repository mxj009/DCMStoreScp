package com.tqhy.dcm4che.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 初始化SCU客户端Data对象,包含来源,类型,部位信息
 *
 * @author Yiheng
 * @create 2018/5/16
 * @since 1.0.0
 */
public class InitScuData implements Serializable{
    private static final long serialVersionUID = 1L;
    private List<String> part;
    private List<String> source;
    private List<String> type;

    public List<String> getPart() {
        return part;
    }

    public void setPart(List<String> part) {
        this.part = part;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "InitScuData{" +
                "part=" + part +
                ", source=" + source +
                ", type=" + type +
                '}';
    }
}

package com.tqhy.dcm4che.msg;

import java.io.Serializable;

/**
 * @author Yiheng
 * @create 2018/5/11
 * @since 1.0.0
 */
public class BaseMsg implements Serializable{
    private static final long serialVersionUID = 1L;
    protected int status;
    protected String desc;
    public static final String UNKNOWN_ERROR = "未知错误";

    public BaseMsg(int status) {
        this.status = status;
    }

    BaseMsg(int status, String desc) {
        this.status =status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "ConnConfigMsg{" +
                "status=" + status +
                ", desc='" + desc + '\'' +
                '}';
    }
}

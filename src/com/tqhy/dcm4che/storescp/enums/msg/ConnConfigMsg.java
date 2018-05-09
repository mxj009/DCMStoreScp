package com.tqhy.dcm4che.storescp.enums.msg;

/**
 * @author Yiheng
 * @create 2018/5/9
 * @since 1.0.0
 */
public enum ConnConfigMsg {
    FAILURE(0,"未知错误"),
    SUCCESS(1,"连接配置成功"),
    PORT_NOT_NUMBER_ERROR(2,"端口号必须为数字"),
    PORT_OUT_RANGE_ERROR(3,"端口号必须为0~65536之间数字"),
    PORT_BE_USED_ERROR(4,"端口号被占用"),
    CONFIG_BLANK_ERROR(5,"配置内容不能为空");

    private int status;
    private String desc;

    ConnConfigMsg(int status, String desc) {
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

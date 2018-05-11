package com.tqhy.dcm4che.storescp.enums.msg;

/**
 * @author Yiheng
 * @create 2018/5/11
 * @since 1.0.0
 */
public enum ExcelOperationMsg {
    SAVE_FAILURE(0,"保存失败"),
    SAVE_PATH_INVALID(1,"保存路径无效"),
    PARSE_FAILURE(2,"文件解析失败"),
    PARSE_SUCCESS(3,"文件解析成功"),
    SAVE_SUCCESS(4,"保存成功" );

    private int status;
    private String desc;

    ExcelOperationMsg(int status, String desc) {
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
        return "ExcelOperationMsg{" +
                "status=" + status +
                ", desc='" + desc + '\'' +
                '}';
    }
}

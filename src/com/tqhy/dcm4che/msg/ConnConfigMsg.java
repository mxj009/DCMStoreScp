package com.tqhy.dcm4che.msg;

import com.tqhy.dcm4che.storescp.configs.ConnectConfig;

/**
 * 连接配置消息
 *
 * @author Yiheng
 * @create 2018/5/9
 * @since 1.0.0
 */
public class ConnConfigMsg extends BaseMsg {

    public static final int CONFIG_SUCCESS = 1;
    public static final int CONFIG_FAIL = 0;
    public static final String CONFIG_SUCCESS_DESC ="SCU连接配置成功";
    public static final String CONFIG_IS_NULL ="SCU连接配置为空";
    public static final String PORT_NOT_NUMBER_ERROR = "端口号必须为数字";
    public static final String CONFIG_FORMAT_NOT_RIGHT = "配置格式错误";
    private static final long serialVersionUID = 1L;

    private ConnectConfig config;

    public ConnConfigMsg(int status) {
        super(status);
    }

    public ConnConfigMsg(int status, String desc) {
        super(status, desc);
    }

    public ConnConfigMsg(int status, ConnectConfig config) {
        super(status);
        this.config = config;
    }

    public ConnConfigMsg(int status, String desc, ConnectConfig config) {
        super(status, desc);
        this.config = config;
    }

    public ConnectConfig getConfig() {
        return config;
    }

    public void setConfig(ConnectConfig config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "ConnConfigMsg{" +
                "config=" + config +
                ", status=" + status +
                ", desc='" + desc + '\'' +
                '}';
    }
}

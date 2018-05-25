package com.tqhy.dcm4che.msg;

import com.tqhy.dcm4che.entity.InitScuData;

/**
 * 初始化客户端source,part,type的msg
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class InitScuMsg extends BaseMsg {

    private static final long serialVersionUID = 1L;
    private InitScuData data;

    public InitScuData getData() {
        return data;
    }

    public void setData(InitScuData data) {
        this.data = data;
    }

    public InitScuMsg(int status) {
        super(status);
    }

    public InitScuMsg(int status, String desc) {
        super(status, desc);
    }

    @Override
    public String toString() {
        return "InitScuMsg{" +
                "data=" + data +
                ", status=" + status +
                ", desc='" + desc + '\'' +
                '}';
    }
}

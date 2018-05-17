package com.tqhy.dcm4che.msg;

import java.io.Serializable;

/**
 *
 * 上传文件信息消息
 * @author Yiheng
 * @create 2018/5/17
 * @since 1.0.0
 */
public class UpLoadInfoMsg extends BaseMsg implements Serializable{

    private static final long serialVersionUID = 1L;

    private int dicomFileCount;

    public UpLoadInfoMsg(int status) {
        super(status);
    }

    public int getDicomFileCount() {
        return dicomFileCount;
    }

    public void setDicomFileCount(int dicomFileCount) {
        this.dicomFileCount = dicomFileCount;
    }
}

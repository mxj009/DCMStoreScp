package com.tqhy.dcm4che.entity;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 上传jpg图片对应
 * @author Yiheng
 * @create 2018/5/30
 * @since 1.0.0
 */
public class UploadJpg implements Serializable{

    private static final long serialVersionUID = 1L;

    private Batch batch;
    private List<String> paths;

    public UploadJpg() {
    }

    public UploadJpg(Batch batch, List<String> paths) {
        this.batch = batch;
        this.paths = paths;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}

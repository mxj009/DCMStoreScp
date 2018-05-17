package com.tqhy.dcm4che.storescp.tasks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class BaseTask {

    protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;

    public void setStream(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
    }

    public BaseTask() {
    }

    public BaseTask(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void  releaseStream(){
        this.in = null;
        this.out = null;
        System.out.println(getClass().getSimpleName() + " releaseStream...");
    }
}

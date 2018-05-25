package com.tqhy.dcm4che.storescp.tasks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * 所有需要与scu客户端进行socket通讯的任务都须继承此类,达到使用同一对ObjectStream的目的.每个任务开始前,
 * 须调用{@link BaseTask#setStream(ObjectInputStream, ObjectOutputStream)}方法获取socket
 * 输入输出流,任务执行完毕,须调用{@link BaseTask#releaseStream()}方法,将本任务持有的输入输出流对象置空,
 * 以达到让下一个任务获取输入输出流对象目的.
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class BaseTask {

    protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;

    /**
     * 获取Socket对应的输入输出流对象
     * @param in
     * @param out
     */
    public BaseTask setStream(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
        return this;
    }

    public BaseTask() {
    }

    public BaseTask(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * 将本任务持有的流对象置空
     */
    public void  releaseStream(){
        this.in = null;
        this.out = null;
        System.out.println(getClass().getSimpleName() + " releaseStream...");
    }
}

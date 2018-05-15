package com.tqhy.dcm4che.storescp.tasks;

/**
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class BaseTask {

    protected int taskType;
    public static final int EXCEL_TASK = 1;
    public static final int INIT_MSG_TO_SCU_TASK = 2;

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }
}

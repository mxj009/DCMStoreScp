package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.AssembledBatch;
import com.tqhy.dcm4che.msg.BaseMsg;
import com.tqhy.dcm4che.msg.ScuCommandMsg;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * 创建批次任务类
 *
 * @author Yiheng
 * @create 2018/5/15
 * @since 1.0.0
 */
public class BatchTask extends BaseTask {

    public AssembledBatch call() {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = in;
            oos = out;
            System.out.println("BatchTask start... " );
            ScuCommandMsg createReadyMsg = new ScuCommandMsg(1);
            createReadyMsg.setCommand(ScuCommandMsg.CREATE_BATCH_READY);
            oos.writeObject(createReadyMsg);
            oos.flush();
            System.out.println("BatchTask ScuCommandMsg.CREATE_BATCH_READY sent...");

            AssembledBatch assembledBatch = (AssembledBatch) ois.readObject();
            System.out.println("BatchTask AssembledBatch read..." + assembledBatch);
            oos.writeObject(new BaseMsg(BaseMsg.SUCCESS));
            oos.flush();
            return assembledBatch;
        } catch (EOFException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            releaseStream();
        }

        return new AssembledBatch();
    }

    public BatchTask() {
    }

}

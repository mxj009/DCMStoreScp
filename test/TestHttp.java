import com.tqhy.dcm4che.storescp.tasks.InitScuTask;
import org.junit.Test;

/**
 * @author Yiheng
 * @create 2018/5/24
 * @since 1.0.0
 */
public class TestHttp {

    @Test
    public void testHttp(){
        InitScuTask initScuTask = new InitScuTask();
        //initScuTask.getInitDataByOkHttp();
        //initScuTask.getInitDataByHttpUrlConnection();
        initScuTask.fakeGetInitData();
    }
}

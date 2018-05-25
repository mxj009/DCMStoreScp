import com.tqhy.dcm4che.storescp.tasks.ImgCenterTask;
import org.junit.Test;

import java.io.File;

/**
 *
 * 测试ImgCenterTask
 *
 * @author Yiheng
 * @create 2018/5/23
 * @since 1.0.0
 */
public class TestImgCenterTask {
    @Test
    public void testDcm2Jpg(){
        File dcmFile = new File("C:/Users/qing/Desktop/临时文件传递/颈椎/1.2.528.1.1001.200.10.573.3977.21946088445.20180427083403140/SDY00000/SRS00000/IMG00000.dcm");
        String imgUrl = ImgCenterTask.getImgUrl(dcmFile);
        System.out.println(imgUrl);
    }
}

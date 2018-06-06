import com.tqhy.dcm4che.storescp.tasks.Dcm2JpgTask;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author Yiheng
 * @create 2018/5/18
 * @since 1.0.0
 */
public class TestPythonExecute {

    /**
     * 测试调用python程序转换Jpg文件到1024像素
     */
    @Test
    public void test1024UrlGenerate() {
        String imgPath = "C:/Users/qing/Desktop/rev/test.jpg";
        URL resource = TestPythonExecute.class.getResource("/img1024Url.py");
        String path = resource.getPath();
        if ('/' == path.charAt(0)) {
            path = path.substring(1);
            System.out.println("subString path: " + path);
        }

        System.out.println(path);
        String[] arguments = new String[]{"python", path, imgPath};
        try {
            Process process = Runtime.getRuntime().exec(arguments);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println("ddd:" + line);
            }
            in.close();
            int re = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试DCM文件转换为jpg格式图片
     */
    @Test
    public void testDcm2Jpg() {
        String dicomPath = "C:/Users/qing/Desktop/dcm_pics/IMG00001";
        File dicomFile = new File(dicomPath);
        Dcm2JpgTask dcm2JpgTask = new Dcm2JpgTask();
        File jpgFile = dcm2JpgTask.convert(dicomFile);
        System.out.println(null == jpgFile ? "fail" : jpgFile.getAbsolutePath());
    }

    /**
     * 获取python文件路径
     */
    @Test
    public void testGetPyPath() {
        URL resource = TestPythonExecute.class.getResource("img1024Url.py");
        //URL resource = TestPythonExecute.class.getResource("/");
        String path = resource.getPath();
        String filePath = resource.getFile();
        System.out.println(path);
        System.out.println(filePath);
    }

    /**
     * 测试解析Python文件路径
     */
    @Test
    public void testParsePath() {
        String originPath = "/D:/workspace/DCMStoreScp/out/artifacts/DCMStoreScp_jar/DCMStoreScp.jar";
        int end = originPath.lastIndexOf("/");
        String substring = originPath.substring(1, end);
        System.out.println(substring);
    }
}

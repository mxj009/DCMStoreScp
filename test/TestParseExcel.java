import com.tqhy.dcm4che.entity.Batch;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.storescp.tasks.ExcelTask;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 测试通过POI对Excel进行处理
 *
 * @author Yiheng
 * @create 2018/5/10
 * @since 1.0.0
 */
public class TestParseExcel {

    private String fpath = "C:/Users/qing/Desktop/报告模板 - 副本.xls";
    private static final String PATIENT_ID = "放射科号";
    private static final String IMG_INFO = "征象描述";
    private static final String IMG_RESULT = "诊断结论";
    private static final String AGE_NUMBER = "年龄";

    @Test
    public void testParseAge() {
        String ageStr = "062Y10M00D";
        double age = ExcelTask.parseAge(ageStr);
        System.out.println(age);
    }

    @Test
    public void testParseSerialNumber() {
        String serialNumber = "1.0";
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(serialNumber);
        if (matcher.find()) {
            serialNumber = matcher.group(0);
        }
        System.out.println(getClass().getSimpleName() + "DicomTag.SERIAL_NUMBER: " + serialNumber);
    }

    /**
     * 测试读取并解析hssf类型Excel(后缀为.xls)
     */
    @Test
    public void testReadHssf() {
        String path = "C:/Users/qing/Desktop/报告模板.xls";
        File file = new File(path);
        ExcelTask excelTask = new ExcelTask();
        excelTask.setBatch(new Batch("aaa", "aaa"));
        String[] columnNames = {PATIENT_ID, AGE_NUMBER};
        List<ImgCase> imgCases = excelTask.parseExcel(file, columnNames);
        System.out.println(imgCases);
    }
}

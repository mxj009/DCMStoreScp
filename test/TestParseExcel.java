
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 测试通过POI对Excel进行处理
 *
 * @author Yiheng
 * @create 2018/5/10
 * @since 1.0.0
 */
public class TestParseExcel {

    private String fpath = "C:/Users/qing/Desktop/报告模板 - 副本.xls";

    /**
     * 测试读取并解析hssf类型Excel(后缀为.xls)
     */
    @Test
    public void readHssf() {
        FileInputStream fis = null;
        HSSFWorkbook wb = null;
        try {
            fis = new FileInputStream(fpath);
            wb = new HSSFWorkbook(fis);
            HSSFSheet sheet = wb.getSheetAt(0);

            int rows = sheet.getPhysicalNumberOfRows();
            System.out.println("Sheet " + 0 + " \"" + wb.getSheetName(0) + "\" has " + rows + " row(s).");

            for (int i = 0; i < rows; i++) {
                HSSFRow row = sheet.getRow(i);
                if (null == row) {
                    continue;
                }
                System.out.println("\nROW " + row.getRowNum() + " has " + row.getPhysicalNumberOfCells() + " cell(s).");
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    HSSFCell cell = row.getCell(c);
                    String value;

                    if (cell != null) {
                        switch (cell.getCellTypeEnum()) {

                            case FORMULA:
                                value = "FORMULA value=" + cell.getCellFormula();
                                break;

                            case NUMERIC:
                                value = "NUMERIC value=" + cell.getNumericCellValue();
                                break;

                            case STRING:
                                value = "STRING value=" + cell.getStringCellValue();
                                break;

                            case BLANK:
                                value = "<BLANK>";
                                break;

                            case BOOLEAN:
                                value = "BOOLEAN value-" + cell.getBooleanCellValue();
                                break;

                            case ERROR:
                                value = "ERROR value=" + cell.getErrorCellValue();
                                break;

                            default:
                                value = "UNKNOWN value of type " + cell.getCellTypeEnum();
                        }
                        System.out.println("CELL col=" + cell.getColumnIndex() + " VALUE=" + value);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != wb) {
                    wb.close();
                }

                if (null != fis) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

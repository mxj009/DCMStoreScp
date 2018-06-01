package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.storescp.utils.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 执行Excel相关操作,包括保存,解析等
 *
 * @author Yiheng
 * @create 2018/5/10
 * @since 1.0.0
 */
public class ExcelTask extends FileTask {
    /**
     * 存放Excel表格中需要解析列的序号
     */
    private Map<Integer, String> columnMap = new HashMap<>();

    private static final String PATIENT_ID = "放射科号";
    private static final String IMG_INFO = "征象描述";
    private static final String IMG_RESULT = "诊断结论";
    private static final String AGE_NUMBER = "年龄";

    public List<ImgCase> call() {
        File savedFile = saveFile();
        String[] columnNames = {PATIENT_ID, IMG_INFO, IMG_RESULT, AGE_NUMBER};
        List<ImgCase> imgCases = parseExcel(savedFile, columnNames);
        return imgCases;
    }

    /**
     * 解析Excel文件,根据每一行内容生成对应的ImgCase对象,最终返回所有ImgCase对象的集合.
     * 解析每个Excel文件的第一步是调用{@link #pickColumn(HSSFSheet, String...)}方法,获取需要解析的列的序号,保存到
     * 成员变量columns中,解析其他行时,将解析到的单元格列序号与map中保存的列序号对比,从而判断单元格是否是需要
     * 获取值得单元格,如果是则将其中内容赋值给该行对应的ImgCase对象.
     *
     * @param file 待解析的Excel文件
     * @return Excel中每一行对应的ImgCase对象的集合
     */
    public List<ImgCase> parseExcel(File file, String... columnNames) {
        System.out.println("ExcelTask parseExcel start...");
        FileInputStream fis = null;
        HSSFWorkbook wb = null;
        try {
            fis = new FileInputStream(file);
            wb = new HSSFWorkbook(fis);
            HSSFSheet sheet = wb.getSheetAt(0);
            pickColumn(sheet, columnNames);
            int rows = sheet.getPhysicalNumberOfRows();
            //System.out.println("Sheet " + 0 + " \"" + wb.getSheetName(0) + "\" has " + rows + " row(s).");
            ArrayList<ImgCase> imgCases = new ArrayList<>();
            for (int i = 1; i < rows; i++) {
                HSSFRow row = sheet.getRow(i);
                if (null == row) {
                    continue;
                }
                //System.out.println("\nROW " + row.getRowNum() + " has " + row.getPhysicalNumberOfCells() + " cell(s).");
                ImgCase aCase = new ImgCase();
                int count = 0;
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    if (count == columnNames.length) {
                        break;
                    }
                    HSSFCell cell = row.getCell(c);
                    String value = null;
                    if (cell != null) {
                        if (CellType.STRING == cell.getCellTypeEnum()) {
                            value = cell.getStringCellValue();
                            String columnName = columnMap.get(c);
                            if (null == columnName) {
                                continue;
                            }
                            if (PATIENT_ID.equals(columnName)) {
                                count++;
                                aCase.setPatientId(value);
                                continue;
                            }

                            if (IMG_INFO.equals(columnName)) {
                                count++;
                                aCase.setImgInfo(value);
                                continue;
                            }
                            if (IMG_RESULT.equals(columnName)) {
                                count++;
                                aCase.setImgResult(value);
                                continue;
                            }
                            if (AGE_NUMBER.equals(columnName)) {
                                count++;
                                double age = parseAge(value);
                                aCase.setAgeNumber(age);
                                continue;
                            }
                        }
                    }
                }
                aCase.setBatchNo(batch.getBatchNo());
                imgCases.add(aCase);
            }
            return imgCases;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
        return null;
    }

    /**
     * 通过比对第一行每一格内的名字判断是否是要解析的列,如果是,则将该列的列名和序号以键值对
     * 的形式存到HashMap中.
     *
     * @param sheet
     * @param columnNames
     * @throws Exception 要解析的列数大于表格实际列数
     */
    private void pickColumn(HSSFSheet sheet, String... columnNames) throws Exception {
        HSSFRow titleRow = sheet.getRow(0);
        short lastCellNum = titleRow.getLastCellNum();
        if (columnNames.length > lastCellNum + 1) {
            throw new Exception("要解析的列数大于表格实际列数");
        }
        int count = 0;
        for (String name : columnNames) {
            if (count == columnNames.length) {
                break;
            }
            for (int i = 0; i <= lastCellNum; i++) {
                String value = titleRow.getCell(i).getStringCellValue();
                if (StringUtils.equals(name, value)) {
                    count++;
                    columnMap.put(i, name);
                    break;
                }
            }
        }
    }

    public static double parseAge(String ageStr) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(ageStr);
        double age = 0d;
        String format = null;
        for (int i = 0; matcher.find(); i++) {
            if (i == 0) {
                age += Double.parseDouble(matcher.group(0));
            } else if (i == 1) {
                age += Double.parseDouble(matcher.group(0)) / 12;
            } else {
                age += Double.parseDouble(matcher.group(0)) / 365;
            }
            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            format = decimalFormat.format(age);
        }
        return Double.parseDouble(format);
    }
}

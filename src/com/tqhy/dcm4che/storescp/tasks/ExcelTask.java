package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.msg.ScuCommandMsg;
import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import com.tqhy.dcm4che.entity.ImgCase;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 执行Excel相关操作,包括保存,解析等
 *
 * @author Yiheng
 * @create 2018/5/10
 * @since 1.0.0
 */
public class ExcelTask extends BaseTask implements Callable<List<ImgCase>> {

    private StorageConfig sdConfig;
    private Socket socket;

    @Override
    public List<ImgCase> call() {
        DataInputStream dis = null;
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ScuCommandMsg transReadyMsg = new ScuCommandMsg(1);
            transReadyMsg.setCommand(ScuCommandMsg.TRANSFER_ECXEL_READY);
            oos.writeObject(transReadyMsg);
            oos.flush();

            System.out.println("ExcelTask excel runnable run...");
            dis = new DataInputStream(socket.getInputStream());
            //获取批次号
            //String batchId = dis.readUTF();
            //System.out.println("ExcelTask excel batchId is: " + batchId);
            //获取批次备注
            //String batchDesc = dis.readUTF();
            //System.out.println("ExcelTask excel batchDesc is: " + batchDesc);
            //获取文件名
            String fname = dis.readUTF();
            System.out.println("ExcelTask excel fname is: " + fname);

            File dir = new File(sdConfig.getDirectory());
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(dir, fname.trim());
            List<ImgCase> imgCases = saveExcel(file, dis);
            return imgCases;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ImgCase> parseExcel(File file) {
        System.out.println("ExcelTask parseExcel start...");
        FileInputStream fis = null;
        HSSFWorkbook wb = null;
        try {
            fis = new FileInputStream(file);
            wb = new HSSFWorkbook(fis);
            HSSFSheet sheet = wb.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            //System.out.println("Sheet " + 0 + " \"" + wb.getSheetName(0) + "\" has " + rows + " row(s).");
            ArrayList<ImgCase> imgCases = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                HSSFRow row = sheet.getRow(i);
                if (null == row) {
                    continue;
                }
                //System.out.println("\nROW " + row.getRowNum() + " has " + row.getPhysicalNumberOfCells() + " cell(s).");
                ImgCase aCase = new ImgCase();
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    HSSFCell cell = row.getCell(c);
                    String value = null;
                    if (cell != null) {
                        if (CellType.STRING == cell.getCellTypeEnum()) {
                            value = cell.getStringCellValue();
                            switch (c) {
                                case 0:
                                    aCase.setPatientId(value);
                                    break;
                                case 1:
                                    aCase.setImgInfo(value);
                                    break;
                                case 2:
                                    aCase.setImgResult(value);
                                    break;
                                case 3:
                                    aCase.setAge(value);
                                    break;
                            }
                        }
                    }
                }
                imgCases.add(aCase);
            }
            return imgCases;
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
        return null;
    }

    private List<ImgCase> saveExcel(File file, DataInputStream dis) {
        System.out.println("ExcelTask will save excel to dir: " + file.getPath());
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            long fileLength = dis.readLong();

            byte[] bytes = new byte[1024 * 8];
            int len = 0;
            while ((len = dis.read(bytes)) != -1 && file.length() < fileLength) {
                System.out.println("ExcelTask writing excel..." + len);
                System.out.println();
                bos.write(bytes, 0, len);
                bos.flush();
                //System.out.println("file.length(): " + file.length() + ", fileLength: " + fileLength);
                if (file.length() == fileLength) {
                    //System.out.println("upload excel complete...");
                    break;
                }
            }
            bos.close();
            List<ImgCase> imgCases = parseExcel(file);
            return imgCases;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StorageConfig getSdConfig() {
        return sdConfig;
    }

    public void setSdConfig(StorageConfig sdConfig) {
        this.sdConfig = sdConfig;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

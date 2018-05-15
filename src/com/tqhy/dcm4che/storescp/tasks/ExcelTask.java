package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import com.tqhy.dcm4che.entity.ImgCase;

import java.io.*;
import java.net.Socket;
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
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write("start trans excel");
            writer.flush();
            System.out.println("excel runnable run...");
            bis = new BufferedInputStream(socket.getInputStream());
            byte[] fnByte = new byte[256];
            bis.read(fnByte);
            String fname = new String(fnByte);
            System.out.println("fname is: " + fname);
            File dir = new File(sdConfig.getDirectory());
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(dir, fname.trim());
            saveExcel(file, bis, bos);
            List<ImgCase> imgCases = parseExcel(file);
            return imgCases;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private List<ImgCase> parseExcel(File file) {
        return null;
    }

    private void saveExcel(File file, BufferedInputStream bis, BufferedOutputStream bos) throws IOException {
        System.out.println("will save excel to dir: " + file.getPath());

        bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bytes = new byte[1024 * 8];
        int len = 0;
        while ((len = bis.read(bytes)) != -1) {
            System.out.println("writing...");
            System.out.println();
            bos.write(bytes);
        }
        bos.flush();
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

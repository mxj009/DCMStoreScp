package com.tqhy.dcm4che.storescp.excel;

import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import com.tqhy.dcm4che.storescp.configs.StorageConfig;

import java.awt.image.DirectColorModel;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Yiheng
 * @create 2018/5/10
 * @since 1.0.0
 */
public class ExcelRunnable implements Runnable {

    private ConnectConfig connectConfig;
    private StorageConfig sdConfig;

    @Override
    public void run() {
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            System.out.println("excel runnable run...");
            ServerSocket serverSocket = new ServerSocket(connectConfig.getPort() + 1);
            Socket accept = serverSocket.accept();
            bis = new BufferedInputStream(accept.getInputStream());
            byte[] fnByte = new byte[256];
            bis.read(fnByte);
            String fname = new String(fnByte);
            System.out.println("fname is: " + fname);
            File dir = new File(sdConfig.getDirectory());
            if (dir.exists()) {
                File file = new File(dir, fname.trim());
                System.out.println("get excel name is: " + file.getName());
                System.out.println("will save excel to dir: " + file.getPath());
                //System.out.println("save excel file to: "+file.getAbsolutePath());

                bos = new BufferedOutputStream(new FileOutputStream(file));
                byte[] bytes = new byte[1024 * 8];
                int len = 0;
                while ((len = bis.read(bytes)) != -1) {
                    System.out.println("writing...");
                    System.out.println();
                    bos.write(bytes);
                }
            } else {
                System.out.println("保存Excel路径不存在...");
            }

            bos.flush();
            bis.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConnectConfig getConnectConfig() {
        return connectConfig;
    }

    public void setConnectConfig(ConnectConfig connectConfig) {
        this.connectConfig = connectConfig;
    }

    public StorageConfig getSdConfig() {
        return sdConfig;
    }

    public void setSdConfig(StorageConfig sdConfig) {
        this.sdConfig = sdConfig;
    }
}

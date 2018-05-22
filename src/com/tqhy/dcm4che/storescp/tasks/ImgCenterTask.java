package com.tqhy.dcm4che.storescp.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * 产生一个Dicom影像文件对应ImgCenter对象的任务
 *
 * @author Yiheng
 * @create 2018/5/18
 * @since 1.0.0
 */
public class ImgCenterTask {
    private File dicomFile;
    private File jpgFile;

    public String getImgUrl() {
        System.out.println(getClass().getSimpleName() + " getImgUrl...");
        Dcm2JpgTask dcm2JpgTask = new Dcm2JpgTask(dicomFile);
        jpgFile = dcm2JpgTask.call();
        if (null != jpgFile && jpgFile.exists()) {
            return jpgFile.getPath();
        }
        return null;
    }

    public String getImgUrlThumb() {
        System.out.println(getClass().getSimpleName() + " getImgUrlThumb...");
        return callPython("/imgUrlThumb.py", "_thumb.jpg");
    }

    public String getImg1024Url() {
        System.out.println(getClass().getSimpleName() + " getImg1024Url...");
        return callPython("/img1024Url.py", "_1024.jpg");
    }

    private String callPython(String pyResource, String target) {
        if (null != jpgFile && jpgFile.exists() && jpgFile.isFile()) {
            String imgPath = jpgFile.getAbsolutePath();
            URL resource = ImgCenterTask.class.getResource(pyResource);
            String pyPath = resource.getPath();
            System.out.println("path is: " + pyPath);
            if ('/' == pyPath.charAt(0)) {
                pyPath = pyPath.substring(1);
                System.out.println("path is: " + pyPath);
            }
            String[] arguments = new String[]{"python", pyPath, imgPath};
            try {
                Process process = Runtime.getRuntime().exec(arguments);
                process.waitFor();
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    System.out.println(getClass().getSimpleName() + " callPython while.." + line);
                    if ("ok".equals(line)) {
                        String newImgPath = imgPath.replace(".jpg", target);
                        System.out.println(getClass().getSimpleName() + " callPython complete... " + newImgPath);
                        return newImgPath;
                    } else {
                        System.out.println(getClass().getSimpleName() + " call Python fail...");
                    }
                }
                in.close();
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ImgCenterTask(File dicomFile) {
        this.dicomFile = dicomFile;
    }
}

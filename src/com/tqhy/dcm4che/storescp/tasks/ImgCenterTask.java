package com.tqhy.dcm4che.storescp.tasks;

import com.tqhy.dcm4che.Main;

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

    public static String getImgUrl(File dicomFile) {
        System.out.println(ImgCenterTask.class.getSimpleName() + " getImgUrl...");
        File jpgFile = Dcm2JpgTask.getInstance().convert(dicomFile);
        if (null != jpgFile && jpgFile.exists()) {
            return jpgFile.getPath();
        }
        return null;
    }

    public static String getImgUrlThumb(File jpgFile) {
        System.out.println(ImgCenterTask.class.getSimpleName() + " getImgUrlThumb...");
        return callPython("/imgUrlThumb.py", "_thumb.jpg", jpgFile);
    }

    public static String getImg1024Url(File jpgFile) {
        System.out.println(ImgCenterTask.class.getSimpleName() + " getImg1024Url...");
        return callPython("/img1024Url.py", "_1024.jpg", jpgFile);
    }

    private static String callPython(String pyResource, String target, File jpgFile) {
        if (null != jpgFile && jpgFile.exists() && jpgFile.isFile()) {
            String imgPath = jpgFile.getAbsolutePath();
            String pyPath = Main.rootPath + pyResource;
            System.out.println("python path is: " + pyPath);
            String[] arguments = new String[]{"python", pyPath, imgPath};
            try {
                Process process = Runtime.getRuntime().exec(arguments);
                process.waitFor();
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    System.out.println(ImgCenterTask.class.getSimpleName() + " callPython while.." + line);
                    if ("ok".equals(line)) {
                        String newImgPath = imgPath.replace(".jpg", target);
                        System.out.println(ImgCenterTask.class.getSimpleName() + " callPython complete... " + newImgPath);
                        return newImgPath;
                    } else {
                        System.out.println(ImgCenterTask.class.getSimpleName() + " call Python fail...");
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

}

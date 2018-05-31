package com.tqhy.dcm4che.storescp.utils;

import com.tqhy.dcm4che.Main;
import com.tqhy.dcm4che.storescp.tasks.Dcm2JpgTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * 与ImgCenter赋值相关的工具类,包括获取缩略图,1024图url等
 *
 * @author Yiheng
 * @create 2018/5/18
 * @since 1.0.0
 */
public class ImgCenterUtils {

    public static String getImgUrlOfDcm(File dicomFile) {
        System.out.println(ImgCenterUtils.class.getSimpleName() + " getImgUrlOfDcm...");
        File jpgFile = Dcm2JpgTask.getInstance().convert(dicomFile);
        if (null != jpgFile && jpgFile.exists()) {
            return jpgFile.getPath();
        }
        return null;
    }

    public static String getImgUrlThumb(File jpgFile) {
        System.out.println(ImgCenterUtils.class.getSimpleName() + " getImgUrlThumb...");
        return callPython("/imgUrlThumb.py", "_thumb.jpg", jpgFile);
    }

    public static String getImg1024Url(File jpgFile) {
        System.out.println(ImgCenterUtils.class.getSimpleName() + " getImg1024Url...");
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
                    System.out.println(ImgCenterUtils.class.getSimpleName() + " callPython while.." + line);
                    if ("ok".equals(line)) {
                        String newImgPath = imgPath.replace(".jpg", target);
                        System.out.println(ImgCenterUtils.class.getSimpleName() + " callPython complete... " + newImgPath);
                        return newImgPath;
                    } else {
                        System.out.println(ImgCenterUtils.class.getSimpleName() + " run Python fail...");
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

package com.tqhy.dcm4che.storescp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author Yiheng
 * @create 2018/5/21
 * @since 1.0.0
 */
public class TestMain {
    public static void main(String[] args) {
        String imgPath = "C:/Users/qing/Desktop/rev/test.jpg";
        URL resource = TestMain.class.getResource("/img1024Url.py");
        String path = resource.getPath();
        System.out.println("path is: " + path);
        if ('/'==path.charAt(0)){
            path = path.substring(1);
        }
        // String[] arguments = new String[]{"python", "C:/Users/qing/Desktop/rev/mypython.py", imgPath};
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
            System.out.println("lll:" + re);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

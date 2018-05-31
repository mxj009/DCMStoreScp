package com.tqhy.dcm4che.storescp.utils;

import com.tqhy.dcm4che.Main;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 处理文件相关工具类
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class FileUtils {

    private static final String FILE_TYPE_JPG = "JPG";
    private static final String FILE_TYPE_DCM = "DCM";

    /**
     * 过滤指定类型的文件:DCM或JPG.
     *
     * @param file    待过滤文件
     * @param imgType 希望匹配的文件类型
     * @return 类型符合返回true, 反之返回false
     */
    public static boolean filtrateImgFile(File file, String imgType) {
        if (FILE_TYPE_DCM.equals(imgType)) {
            byte[] bytes = new byte[132];
            try (FileInputStream in = new FileInputStream(file)) {
                int len = readAvailable(in, bytes, 0, 132);
                return 132 == len && bytes[128] == 'D' && bytes[129] == 'I' && bytes[130] == 'C' && bytes[131] == 'M';
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (FILE_TYPE_JPG.equals(imgType)) {
            byte[] bytes = new byte[4];
            try (FileInputStream in = new FileInputStream(file)) {
                int len = readAvailable(in, bytes, 0, 4);
                return 4 == len && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF && (bytes[3] & 0xFF) == 0xE0;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 判断是否可读取指定长度信息
     *
     * @param in
     * @param b   要读取的字节数组
     * @param off 开始位置偏移量
     * @param len 读取最大长度
     * @return 读取到长度
     * @throws IOException
     */
    public static int readAvailable(InputStream in, byte b[], int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        int wpos = off;
        while (len > 0) {
            int count = in.read(b, wpos, len);
            if (count < 0) {
                break;
            }
            wpos += count;
            len -= count;
        }
        return wpos - off;
    }

    /**
     * 获取指定路径下指定类型的所有影像文件
     *
     * @param path     指定的路径
     * @param fileList 获取到的文件集合
     * @param imgType  待获取的文件类型
     * @return 获取到的文件集合
     */
    public static List<File> getFiles(File path, List<File> fileList, String imgType) {
        if (path.exists()) {
            if (path.isDirectory()) {
                File[] files = path.listFiles();
                for (File file : files) {
                    getFiles(file, fileList, imgType);
                }
            } else {
                if (filtrateImgFile(path, imgType)) {
                    if (!path.getName().equals("DICOMDIR")) {
                        fileList.add(path);
                    }
                    return fileList;
                }
            }
        }
        return fileList;
    }

    public static File compressZipFiles(List<File> originFiles) {
        String zipFilePath = Main.rootPath + "/" + System.currentTimeMillis() + ".zip";
        File zipFile = new File(zipFilePath);
        if (zipFile.exists()) {
            zipFile.delete();
        } else {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileInputStream fis = null;
        ZipOutputStream zos = null;
        ZipEntry zipEntry = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for (File file : originFiles) {
                fis = new FileInputStream(file);
                zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);

                int len = 0;
                byte[] bytes = new byte[1024 * 8];
                while ((len = fis.read(bytes)) != -1) {
                    zos.write(bytes, 0, len);
                }
                zos.flush();
            }
            zipEntry.clone();
            zos.close();
            fis.close();
            return zipFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> extractZipFiles(File file, File dest) {
        List<String> paths = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(file);
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                //System.out.println("extract file :" + entry.getName());
                String destDir = null;
                if (null == dest) {
                    destDir = file.getParent() + "/";
                } else if (dest.isDirectory()) {
                    if (!dest.exists()) {
                        dest.mkdir();
                    }
                    destDir = dest.getAbsolutePath() + "/";
                }
                //System.out.println("extract file to: "+destDir);
                File outFile = new File(destDir + entry.getName());
                //System.out.println("extract file outFile is: "+outFile.getAbsolutePath());
                if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdir();
                }
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
                byte[] bytes = new byte[1024 * 8];
                int len = 0;
                while ((len = bis.read(bytes)) != -1) {
                    bos.write(bytes, 0, len);
                }
                bis.close();
                bos.close();
                paths.add(outFile.getAbsolutePath());
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * 获取一个文件的md5值
     *
     * @return md5 value
     */
    public static String getMD5(File file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFileNameWithoutSuffix(String path) {
        String name = new File(path).getName();
        name = name.substring(0, name.indexOf('.'));
        return name;
    }
}

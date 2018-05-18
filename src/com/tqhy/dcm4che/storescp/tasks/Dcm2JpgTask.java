package com.tqhy.dcm4che.storescp.tasks;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.image.BufferedImageUtils;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * 将Dicom文件转换为Jpg文件任务
 *
 * @author Yiheng
 * @create 2018/5/18
 * @since 1.0.0
 */
public class Dcm2JpgTask {

    private File dicomFile;
    private int windowIndex;
    private int voiLUTIndex;
    private float windowWidth;
    private float windowCenter;
    private Attributes prState;
    private boolean autoWindowing = true;
    private boolean preferWindow = true;
    private int overlayGrayscaleValue = 0xffff;
    private int overlayActivationMask = 0xffff;
    private ImageWriteParam imageWriteParam;
    private ImageWriter imageWriter;
    private final ImageReader imageReader = ImageIO.getImageReadersByFormatName("DICOM").next();

    public File call() {
        initImageWriter();
        File converted = convert();
        return converted;
    }

    /**
     * 初始化ImageWriter
     */
    public void initImageWriter() {
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("JPEG");
        if (!imageWriters.hasNext()) {
            throw new IllegalArgumentException("formatNotSupported");
        }
        imageWriter = imageWriters.next();
        imageWriteParam = imageWriter.getDefaultWriteParam();
    }

    /**
     * 转换DCM文件到同一文件夹下jpg文件夹下
     * @return 转换后的jgp文件File对象
     */
    public File convert() {
        if (null == dicomFile) {
            return null;
        }
        if (dicomFile.exists()) {
            try {
                File jpgDir = new File(dicomFile.getParent() + "/jpg");
                if (!jpgDir.exists()) {
                    jpgDir.mkdir();
                }
                File dest = new File(jpgDir, dicomFile.getName() + ".jpg");
                ImageInputStream iis = ImageIO.createImageInputStream(dicomFile);
                imageReader.setInput(iis);
                BufferedImage bi = imageReader.read(0, readParam());
                ColorModel cm = bi.getColorModel();
                if (cm.getNumComponents() == 3) {
                    bi = BufferedImageUtils.convertToIntRGB(bi);
                }

                if (dest.exists()) {
                    dest.delete();
                }
                ImageOutputStream ios = ImageIO.createImageOutputStream(dest);
                try {
                    imageWriter.setOutput(ios);
                    imageWriter.write(null, new IIOImage(bi, null, null), imageWriteParam);
                    return dest;
                } finally {
                    try {
                        ios.close();
                        iis.close();
                    } catch (IOException ignore) {
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 设置DicomImageReadParam 数
     * @return
     */
    private ImageReadParam readParam() {
        DicomImageReadParam param = (DicomImageReadParam) imageReader.getDefaultReadParam();
        param.setWindowCenter(windowCenter);
        param.setWindowWidth(windowWidth);
        param.setAutoWindowing(autoWindowing);
        param.setWindowIndex(windowIndex);
        param.setVOILUTIndex(voiLUTIndex);
        param.setPreferWindow(preferWindow);
        param.setPresentationState(prState);
        param.setOverlayActivationMask(overlayActivationMask);
        param.setOverlayGrayscaleValue(overlayGrayscaleValue);
        return param;
    }

    public Dcm2JpgTask(File dicomFile) {
        this.dicomFile = dicomFile;
    }

    public File getDicomFile() {
        return dicomFile;
    }

    public void setDicomFile(File dicomFile) {
        this.dicomFile = dicomFile;
    }

    public int getWindowIndex() {
        return windowIndex;
    }

    public void setWindowIndex(int windowIndex) {
        this.windowIndex = windowIndex;
    }

    public int getVoiLUTIndex() {
        return voiLUTIndex;
    }

    public void setVoiLUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = voiLUTIndex;
    }

    public float getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public float getWindowCenter() {
        return windowCenter;
    }

    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public Attributes getPrState() {
        return prState;
    }

    public void setPrState(Attributes prState) {
        this.prState = prState;
    }

    public boolean isAutoWindowing() {
        return autoWindowing;
    }

    public void setAutoWindowing(boolean autoWindowing) {
        this.autoWindowing = autoWindowing;
    }

    public boolean isPreferWindow() {
        return preferWindow;
    }

    public void setPreferWindow(boolean preferWindow) {
        this.preferWindow = preferWindow;
    }

    public int getOverlayGrayscaleValue() {
        return overlayGrayscaleValue;
    }

    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    public int getOverlayActivationMask() {
        return overlayActivationMask;
    }

    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    public ImageWriteParam getImageWriteParam() {
        return imageWriteParam;
    }

    public void setImageWriteParam(ImageWriteParam imageWriteParam) {
        this.imageWriteParam = imageWriteParam;
    }

    public ImageWriter getImageWriter() {
        return imageWriter;
    }

    public void setImageWriter(ImageWriter imageWriter) {
        this.imageWriter = imageWriter;
    }

    public ImageReader getImageReader() {
        return imageReader;
    }
}

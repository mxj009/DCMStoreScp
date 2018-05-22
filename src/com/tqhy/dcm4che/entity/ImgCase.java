package com.tqhy.dcm4che.entity;

import java.util.List;
import java.util.Objects;

/**
 * 病例实体类
 *
 * @author Yiheng
 * @create 2018/5/10
 * @since 1.0.0
 */
public class ImgCase {

    /**
     * 影像采集日期
     * (0008,0022)
     */
    private String acquisitionDate;

    /**
     * 患者ID,Excel与影像tag关联项
     * (0010,0020)
     */
    private String patientId;

    /**
     * 征象描述
     * tag无对应内容,通过Excel获取
     */
    private String imgInfo;

    /**
     * 诊断结论
     * tag无对应内容,通过Excel获取
     */
    private String imgResult;

    /**
     * 患者年龄出生日期
     * (0010,0030)
     */
    private String age;

    /**
     * 患者年龄数值,由Excel得出
     */
    private double ageNumber;

    /**
     * 患者姓名
     * (0010,0010)
     */
    private String name;

    /**
     * 患者性别
     * (0010,0040)
     */
    private String sex;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 影像数量
     */
    private int imgCount;

    /**
     * 来源
     */
    private String source;

    /**
     * 类型
     */
    private String type;

    /**
     * 部位
     */
    private String part;

    /**
     * 病例对应的影像集合
     */
    private List<ImgCenter> imgCenters;

    /**
     * 病例序列号
     */
    private int serialNumber;

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }


    public List<ImgCenter> getImgCenters() {
        return imgCenters;
    }

    public void setImgCenters(List<ImgCenter> imgCenters) {
        this.imgCenters = imgCenters;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public int getImgCount() {
        return imgCount;
    }

    public void setImgCount(int imgCount) {
        this.imgCount = imgCount;
    }

    public String getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(String acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getImgInfo() {
        return imgInfo;
    }

    public void setImgInfo(String imgInfo) {
        this.imgInfo = imgInfo;
    }

    public String getImgResult() {
        return imgResult;
    }

    public void setImgResult(String imgResult) {
        this.imgResult = imgResult;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public double getAgeNumber() {
        return ageNumber;
    }

    public void setAgeNumber(double ageNumber) {
        this.ageNumber = ageNumber;
    }

    public void setFields(ImgCase newCase) {
        this.setImgCount(this.getImgCount()+newCase.getImgCount());
        this.setImgCenters(newCase.getImgCenters());
        this.setSerialNumber(newCase.getSerialNumber());
        this.setBatchNo(newCase.getBatchNo());
        this.setPart(newCase.getPart());
        this.setType(newCase.getType());
        this.setSource(newCase.getSource());
        this.setName(newCase.getName());
        this.setAge(newCase.getAge());
        this.setSex(newCase.getSex());
        this.setAcquisitionDate(newCase.getAcquisitionDate());
    }

    @Override
    public String toString() {
        return "ImgCase{" +
                "acquisitionDate='" + acquisitionDate + '\'' +
                ", patientId='" + patientId + '\'' +
                ", imgInfo='" + imgInfo + '\'' +
                ", imgResult='" + imgResult + '\'' +
                ", age='" + age + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", imgCount=" + imgCount +
                ", source='" + source + '\'' +
                ", type='" + type + '\'' +
                ", part='" + part + '\'' +
                ", imgCenters=" + imgCenters +
                ", serialNumber=" + serialNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImgCase imgCase = (ImgCase) o;
        return serialNumber == imgCase.serialNumber &&
                Objects.equals(acquisitionDate, imgCase.acquisitionDate) &&
                Objects.equals(patientId, imgCase.patientId) &&
                Objects.equals(age, imgCase.age) &&
                Objects.equals(name, imgCase.name) &&
                Objects.equals(sex, imgCase.sex) &&
                Objects.equals(batchNo, imgCase.batchNo) &&
                Objects.equals(source, imgCase.source) &&
                Objects.equals(type, imgCase.type) &&
                Objects.equals(part, imgCase.part);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acquisitionDate, patientId, imgInfo, imgResult, age, name, sex, batchNo, source, type, part, serialNumber);
    }
}

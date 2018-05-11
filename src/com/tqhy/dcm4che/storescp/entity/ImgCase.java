package com.tqhy.dcm4che.storescp.entity;

/**
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
     * 患者年龄,指的是做检查时年龄,而非当前实际年龄
     * (0010,1010)
     */
    private String age;

    /**
     * 患者年龄数值,需要换算,tag中直接获取的为出生年月日
     * (0010,0030)
     */
    private double ageNum;

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
     * 影像拍摄位置
     * (0018,0015)
     */
    private String part;

    /**
     * 影像路径
     */
    private String imgUrl;

    /**
     * 影像缩略图路径
     */
    private String imgThumbUrl;

    /**
     * 1024像素影像路径
     */
    private String img1024Url;

    /**
     * 影像MD5
     */
    private String md5;

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

    public double getAgeNum() {
        return ageNum;
    }

    public void setAgeNum(double ageNum) {
        this.ageNum = ageNum;
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

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

}

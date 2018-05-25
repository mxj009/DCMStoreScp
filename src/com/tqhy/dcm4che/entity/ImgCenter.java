package com.tqhy.dcm4che.entity;

/**
 * ImgCenter表对应实体类
 *
 * @author Yiheng
 * @create 2018/5/16
 * @since 1.0.0
 */
public class ImgCenter {
    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 影像路径
     */
    private String imgUrl;

    /**
     * 影像缩略图路径
     */
    private String imgUrlThumb;

    /**
     * 1024像素影像路径
     */
    private String img1024Url;

    /**
     * 影像MD5
     */
    private String imgMd5;

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgUrlThumb() {
        return imgUrlThumb;
    }

    public void setImgUrlThumb(String imgUrlThumb) {
        this.imgUrlThumb = imgUrlThumb;
    }

    public String getImg1024Url() {
        return img1024Url;
    }

    public void setImg1024Url(String img1024Url) {
        this.img1024Url = img1024Url;
    }

    public String getImgMd5() {
        return imgMd5;
    }

    public void setImgMd5(String imgMd5) {
        this.imgMd5 = imgMd5;
    }
}

package com.tqhy.dcm4che.storescp.configs;

/**
 * @author Yiheng
 * @create 2018/5/8
 * @since 1.0.0
 */
public class StorageDirectoryConfig {

    private boolean ignore;
    private String directory;

    /**
     * 文件存储路径模式,e.g: 当filePath值为<i>{00100020}/{0020000D}/{0020000E}/{00080018}.dcm</i>,则最终保存的文件位于
     * <i>{00100020}/{0020000D}/{0020000E}/</i>路径下,即<i>Patient ID/Study Instance UID/Series Instance UID/</i>
     * 路径下, 名为{00080018}.dcm,即文件的SOP Instance UID加<i>.dcm</i>后缀.
     * 默认情况下,从客户端接收到的文件会被存储在工作路径下,名为<i>{00080018}</i>,即SOP Instance UID,没有后缀.
     */
    private String filePath;
    public static final String DEFAULT_DIRECTORY = ".";

    public StorageDirectoryConfig() {
    }

    public StorageDirectoryConfig(boolean ignore, String directory, String filePath) {
        this.ignore = ignore;
        this.directory = directory;
        this.filePath = filePath;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

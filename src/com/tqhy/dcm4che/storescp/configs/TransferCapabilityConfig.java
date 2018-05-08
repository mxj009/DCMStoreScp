package com.tqhy.dcm4che.storescp.configs;

/**
 * @author Yiheng
 * @create 2018/5/8
 * @since 1.0.0
 */
public class TransferCapabilityConfig {

    public static final String DEFAULT_SOP_CLASS_PATH = "resource:sop-classes.properties";
    private boolean acceptUnknown;
    private String sopClassesPath;

    public boolean isAcceptUnknown() {
        return acceptUnknown;
    }

    public void setAcceptUnknown(boolean acceptUnknown) {
        this.acceptUnknown = acceptUnknown;
    }

    public String getSopClassesPath() {
        return sopClassesPath;
    }

    public void setSopClassesPath(String sopClassesPath) {
        this.sopClassesPath = sopClassesPath;
    }
}

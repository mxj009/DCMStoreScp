package com.tqhy.dcm4che.msg;

/**
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class ScuCommandMsg extends BaseMsg{

    private static final long serialVersionUID = 1L;
    private int command;
    public static final int GET_ALL_INIT_INFO = 2001;
    public static final int GET_PARTS = 2002;
    public static final int GET_SOURCES = 2003;
    public static final int GET_TYPES = 2004;
    public static final int TRANSFER_ECXEL_REQUEST = 2005;
    public static final int TRANSFER_ECXEL_READY = 2006;

    public ScuCommandMsg(int status) {
        super(status);
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }
}

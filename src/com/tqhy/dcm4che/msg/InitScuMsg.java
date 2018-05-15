package com.tqhy.dcm4che.msg;

import com.tqhy.dcm4che.entity.Part;
import com.tqhy.dcm4che.entity.Source;
import com.tqhy.dcm4che.entity.Type;

import java.util.List;

/**
 * 初始化客户端source,part,type的msg
 *
 * @author Yiheng
 * @create 2018/5/14
 * @since 1.0.0
 */
public class InitScuMsg extends BaseMsg {

    private static final long serialVersionUID = 1L;
    private List<Part> part;
    private List<Source> source;
    private List<Type> type;

    public InitScuMsg(int status) {
        super(status);
    }

    public InitScuMsg(int status, String desc) {
        super(status, desc);
    }

    public List<Part> getPart() {
        return part;
    }

    public void setPart(List<Part> part) {
        this.part = part;
    }

    public List<Source> getSource() {
        return source;
    }

    public void setSource(List<Source> source) {
        this.source = source;
    }

    public List<Type> getType() {
        return type;
    }

    public void setType(List<Type> type) {
        this.type = type;
    }


}

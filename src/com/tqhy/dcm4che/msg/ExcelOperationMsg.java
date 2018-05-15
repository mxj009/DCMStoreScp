package com.tqhy.dcm4che.msg;

/**
 * @author Yiheng
 * @create 2018/5/11
 * @since 1.0.0
 */
public class ExcelOperationMsg extends BaseMsg {

    private static final long serialVersionUID = 1L;

    //SAVE_FAILURE(0,"保存失败"),
    //SAVE_PATH_INVALID(1,"保存路径无效"),
    //PARSE_FAILURE(2,"文件解析失败"),
    //PARSE_SUCCESS(3,"文件解析成功"),
    //SAVE_SUCCESS(4,"保存成功" );

    ExcelOperationMsg(int status, String desc) {
        super(status, desc);
    }


}

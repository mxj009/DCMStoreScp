package com.tqhy.dcm4che.storescp.test;

import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 测试DCM转换为json
 *
 * @author Yiheng
 * @create 2018/5/9
 * @since 1.0.0
 */
public class TestDCM2Json {

    public static void main(String[] args){
        try {
            DicomInputStream dis = new DicomInputStream(new File("C:/Users/qing/Desktop/dcm_pics/IMG00001"));
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            Map<String, ?> conf = new HashMap(2);
            JsonGenerator jsonGen = Json.createGeneratorFactory(conf).createGenerator(new BufferedOutputStream(new FileOutputStream("C:/Users/qing/Desktop/dcm_pics2/222.json")));
            JSONWriter jsonWriter = new JSONWriter(jsonGen);
            dis.setDicomInputHandler(jsonWriter);
            dis.readDataset(-1, -1);
            jsonGen.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

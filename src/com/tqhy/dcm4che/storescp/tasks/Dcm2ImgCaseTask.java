package com.tqhy.dcm4che.storescp.tasks;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.storescp.constant.DicomTag;
import com.tqhy.dcm4che.storescp.utils.StringUtils;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Yiheng
 * @create 2018/5/15
 * @since 1.0.0
 */
public class Dcm2ImgCaseTask extends BaseTask implements Callable<ImgCase> {

    private File dcmFile;

    @Override
    public ImgCase call() throws Exception {
        try {
            File jsonFile = dicom2Json(dcmFile);
            ImgCase imgCase = Json2Object(jsonFile);
            System.out.println(imgCase);
            return imgCase;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File dicom2Json(File dcmFile) {
        try {
            DicomInputStream dis = new DicomInputStream(dcmFile);
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            Map<String, ?> conf = new HashMap(2);
            String jsonFilePath = dcmFile.getAbsolutePath() + ".json";
            System.out.println("Dcm2ImgCaseTask dicom2Json jsonFilePath is: "+jsonFilePath);
            File jsonFile = new File(jsonFilePath);
            jsonFile.createNewFile();
            JsonGenerator jsonGen = Json.createGeneratorFactory(conf)
                    .createGenerator(new BufferedOutputStream(new FileOutputStream(jsonFile)));
            JSONWriter jsonWriter = new JSONWriter(jsonGen);
            dis.setDicomInputHandler(jsonWriter);
            dis.readDataset(-1, -1);
            jsonGen.flush();
            return jsonFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ImgCase Json2Object(File jsonFile) throws FileNotFoundException {
        FileReader reader = new FileReader(jsonFile);
        Type type = new TypeToken<Map<String, Map<String, Object>>>() {
        }.getType();
        Map<String, Map<String, Object>> fromJson = new Gson().fromJson(reader, type);
        System.out.println(fromJson.size());
        ImgCase imgCase = new ImgCase();
        a:
        for (Map.Entry<String, Map<String, Object>> entry : fromJson.entrySet()) {
            String key = entry.getKey();
            System.out.println("key is: " + key);

            switch (key) {
                case DicomTag.ACQUISITION_DATE:
                    String acquisitionDate = getValue(entry);
                    imgCase.setAcquisitionDate(acquisitionDate);
                    break;
                case DicomTag.PATIENT_ID:
                    String id = getValue(entry);
                    imgCase.setPatientId(id);
                    break;
                case DicomTag.SEX:
                    imgCase.setSex(getValue(entry).toString());
                    break;
                case DicomTag.NAME:
                    String nameStr = getValue(entry);
                    String[] split = StringUtils.split(nameStr, '=');
                    String name = split[1].replace('}', ' ').trim();
                    imgCase.setName(name);
                    break;
                case DicomTag.AGE_NUM:
                    String birthDate = getValue(entry);
                    System.out.println(birthDate);
                    imgCase.setAgeNum(0);
                    break;
                case DicomTag.AGE:
                    String age = getValue(entry);
                    imgCase.setAge(age);
                    break a;
            }
        }
        return imgCase;
    }

    private String getValue(Map.Entry<String, Map<String, Object>> entry) {
        Map<String, Object> tagValue = entry.getValue();
        List values = (List) tagValue.get("Value");
        String value = values.get(0).toString();
        return value;
    }

    public Dcm2ImgCaseTask(File dcmFile) {
        this.dcmFile = dcmFile;
    }
}

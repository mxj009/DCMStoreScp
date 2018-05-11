import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tqhy.dcm4che.storescp.constant.DicomTag;
import com.tqhy.dcm4che.storescp.entity.ImgCase;
import com.tqhy.dcm4che.storescp.utils.StringUtils;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.json.JSONWriter;
import org.junit.Test;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 测试DCM转换为json
 *
 * @author Yiheng
 * @create 2018/5/9
 * @since 1.0.0
 */
public class TestDCM2Json {

    private String dcmFilePath = "C:/Users/qing/Desktop/dcm_pics/IMG00001";
    private String jsonFilePath = "C:/Users/qing/Desktop/dcm_pics2/111.json";

    @Test
    public void testDcm2JsonFile() {
        try {
            DicomInputStream dis = new DicomInputStream(new File(dcmFilePath));
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            Map<String, ?> conf = new HashMap(2);
            JsonGenerator jsonGen = Json.createGeneratorFactory(conf)
                    .createGenerator(new BufferedOutputStream(new FileOutputStream(jsonFilePath)));
            JSONWriter jsonWriter = new JSONWriter(jsonGen);
            dis.setDicomInputHandler(jsonWriter);
            dis.readDataset(-1, -1);
            jsonGen.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDcm2JsonObject() {
        try {
            FileReader reader = new FileReader(jsonFilePath);

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
                        break ;
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
            System.out.println(imgCase);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getValue(Map.Entry<String, Map<String, Object>> entry) {
        Map<String, Object> tagValue = entry.getValue();
        List values = (List) tagValue.get("Value");
        String value = values.get(0).toString();
        return value;
    }
}

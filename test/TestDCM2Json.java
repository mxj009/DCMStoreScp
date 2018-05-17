import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tqhy.dcm4che.entity.UploadCase;
import com.tqhy.dcm4che.entity.Batch;
import com.tqhy.dcm4che.entity.ImgCenter;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.storescp.constant.DicomTag;
import com.tqhy.dcm4che.msg.InitScuMsg;
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
            ImgCenter uploadCase = new ImgCenter();
            a:
            for (Map.Entry<String, Map<String, Object>> entry : fromJson.entrySet()) {
                String key = entry.getKey();
                System.out.println("key is: " + key);

                switch (key) {
                    case DicomTag.ACQUISITION_DATE:
                        String acquisitionDate = getValue(entry);
                       // uploadCase.setAcquisitionDate(acquisitionDate);
                        break;
                    case DicomTag.PATIENT_ID:
                        String id = getValue(entry);
                        //uploadCase.setPatientId(id);
                        break;
                    case DicomTag.SEX:
                       // uploadCase.setSex(getValue(entry).toString());
                        break;
                    case DicomTag.NAME:
                        String nameStr = getValue(entry);
                        String[] split = StringUtils.split(nameStr, '=');
                        String name = split[1].replace('}', ' ').trim();
                        //uploadCase.setName(name);
                        break;
                    case DicomTag.SERIAL_NUMBER:
                        String serialNumber = getValue(entry);
                        System.out.println(serialNumber);
                       // uploadCase.setSerialNumber(Integer.parseInt(serialNumber));
                        break;
                    case DicomTag.AGE:
                        String age = getValue(entry);
                       // uploadCase.setAge(age);
                        break a;
                }
            }
            System.out.println(uploadCase);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseTalkScuMsg() {
        String msg = "{\"part\":[\"床旁\",\"ISIC\",\"曲面\",\"胸部\",\"主动脉\",\"口腔\",\"骨肌\"],\"source\":[\"主动脉窦\",\"胡总\",\"其它\",\"北医骨肌\",\"数据资料\",\"北大口腔\",\"安贞\",\"许玉峰老师\",\"双桥医院\",\"皮肤病\"],\"type\":[\"CT\",\"DR\"],\"status\":\"1\",\"desc\":\"查询成功！\"}";
        InitScuMsg initScuMsg = new Gson().fromJson(msg, InitScuMsg.class);

        System.out.println(initScuMsg.getStatus());
        System.out.println(initScuMsg.getDesc());
        System.out.println(initScuMsg.getData().getPart());
        System.out.println(initScuMsg.getData().getType());
        System.out.println(initScuMsg.getData().getSource());
    }

    private String getValue(Map.Entry<String, Map<String, Object>> entry) {
        Map<String, Object> tagValue = entry.getValue();
        List values = (List) tagValue.get("Value");
        String value = values.get(0).toString();
        return value;
    }

    @Test
    public void testObjetToJson() {
        UploadCase assembledCases = new UploadCase();
        Batch batch = new Batch("1234567", "batch desc");
        assembledCases.setBatch(batch);
        ArrayList<ImgCase> cases = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            ImgCase aCase = new ImgCase();
            aCase.setPatientId("patientId" + i);
            aCase.setSex(i % 2 == 0 ? "F" : "M");
            aCase.setAcquisitionDate("2018042" + i);
            aCase.setName("name" + i);
            aCase.setAge("i");
            aCase.setImgResult("img result" + i);
            aCase.setImgInfo("img info" + i);
            aCase.setBatchNo(batch.getBatchNo());
           /* aCase.setImg1024Url("img 1024 url" + i);
            aCase.setImgCount(i);
            aCase.setImgUrl("img url" + i);
            aCase.setImgUrlThumb("img thumb url" + i);
            aCase.setImgMd5("img md5" + i);
            aCase.setSerialNumber(i);*/
            cases.add(aCase);
        }

        assembledCases.setData(cases);
        String json = new Gson().toJson(assembledCases);
        System.out.println(json);
    }
}

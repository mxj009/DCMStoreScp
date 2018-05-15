import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tqhy.dcm4che.storescp.constant.DicomTag;
import com.tqhy.dcm4che.entity.ImgCase;
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

    @Test
    public void testParseTalkScuMsg(){
        String msg = "{\"status\":1,\"desc\":\"message description\",\"part\":[{\"createTime\":1522222434000,\"delFlag\":1,\"id\":\"2c28c4b79af8445db766515c323a5aff\",\"name\":\"??\",\"updateTime\":1522222434000},{\"createTime\":1503968936000,\"delFlag\":1,\"id\":\"5b571a16d61b42f7a7f5e8b9076605f8\",\"name\":\"??\",\"updateTime\":1503968936000},{\"createTime\":1515566629000,\"delFlag\":1,\"id\":\"5bd8be85670d4cfba7f141e9fb050ec9\",\"name\":\"???\",\"updateTime\":1515566629000},{\"createTime\":1499826936000,\"delFlag\":1,\"id\":\"867d657ec68b447ab57f3dff6c3cf576\",\"name\":\"??\",\"updateTime\":1499826936000},{\"createTime\":1514343220000,\"delFlag\":1,\"id\":\"9f19e6bc41d148739a2d7b2dda228030\",\"name\":\"??\",\"updateTime\":1514343220000},{\"createTime\":1523177588000,\"delFlag\":1,\"id\":\"cab76a4dcc3b4b75977e2b9f59ad9031\",\"name\":\"ISIC\",\"updateTime\":1523177588000},{\"createTime\":1505125602000,\"delFlag\":1,\"id\":\"f530443b1a1947799638b15805f35269\",\"name\":\"??\",\"updateTime\":1505125602000}],\"source\":[{\"createTime\":1523177723000,\"delFlag\":1,\"id\":\"3525de9456e54607b5ccf0aad18920ec\",\"name\":\"???\",\"updateTime\":1523177723000},{\"createTime\":1499824161000,\"delFlag\":1,\"id\":\"3f8b081edd0c4060805bf6a077f30679\",\"name\":\"????\",\"updateTime\":1499824161000},{\"createTime\":1512365180000,\"delFlag\":1,\"id\":\"43172961c8eb44a1854499576af10db5\",\"name\":\"?????\",\"updateTime\":1512365180000},{\"createTime\":1505125718000,\"delFlag\":1,\"id\":\"66ddbafe669245f4b58c2c175f435e94\",\"name\":\"??\",\"updateTime\":1505125718000},{\"createTime\":1514342971000,\"delFlag\":1,\"id\":\"8310f5b97fe54a6495688263fa6ca928\",\"name\":\"????\",\"updateTime\":1514342971000},{\"createTime\":1515551362000,\"delFlag\":1,\"id\":\"87670ceda5ee42299258a9fdf5c361bd\",\"name\":\"????\",\"updateTime\":1515551362000},{\"createTime\":1522378201000,\"delFlag\":1,\"id\":\"c1d0878591104f28b2ca2e6dc4cc5bb9\",\"name\":\"????\",\"updateTime\":1522378201000},{\"createTime\":1503969044000,\"delFlag\":1,\"id\":\"dfbee262e15b46d8bc08d1532996bc15\",\"name\":\"??\",\"updateTime\":1503969044000},{\"createTime\":1500520307000,\"delFlag\":1,\"id\":\"e36fbde330594cd6a8d9e5c66551d12d\",\"name\":\"??\",\"updateTime\":1500520307000},{\"createTime\":1515565829000,\"delFlag\":1,\"id\":\"f887334808594a478f65a10925e8e601\",\"name\":\"????\",\"updateTime\":1515565829000}],\"type\":[{\"createTime\":1499826937000,\"delFlag\":1,\"id\":\"6c77cb00e3e743bc963ed71f1a2f5082\",\"name\":\"DR\",\"updateTime\":1499826937000},{\"createTime\":1508479424000,\"delFlag\":1,\"id\":\"de38edcc7ff1461ab8d2185ef6d66ad4\",\"name\":\"CT\",\"updateTime\":1508479424000}]}";
        InitScuMsg initScuMsg = new Gson().fromJson(msg, InitScuMsg.class);

        System.out.println(initScuMsg.getStatus());
        System.out.println(initScuMsg.getDesc());
        System.out.println(initScuMsg.getPart());
        System.out.println(initScuMsg.getType());
        System.out.println(initScuMsg.getSource());
    }

    private String getValue(Map.Entry<String, Map<String, Object>> entry) {
        Map<String, Object> tagValue = entry.getValue();
        List values = (List) tagValue.get("Value");
        String value = values.get(0).toString();
        return value;
    }
}

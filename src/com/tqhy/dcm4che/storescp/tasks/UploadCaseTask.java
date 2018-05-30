package com.tqhy.dcm4che.storescp.tasks;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tqhy.dcm4che.entity.AssembledBatch;
import com.tqhy.dcm4che.entity.ImgCase;
import com.tqhy.dcm4che.entity.ImgCenter;
import com.tqhy.dcm4che.entity.UploadCase;
import com.tqhy.dcm4che.storescp.constant.DicomTag;
import com.tqhy.dcm4che.storescp.utils.MD5Utils;
import com.tqhy.dcm4che.storescp.utils.StringUtils;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yiheng
 * @create 2018/5/15
 * @since 1.0.0
 */
public class UploadCaseTask extends BaseTask implements Callable<UploadCase> {

    private AssembledBatch assembledBatch;
    private File dcmFile;
    private UploadCase uploadCase;

    @Override
    public UploadCase call() {
        try {
            File jsonFile = dicom2Json(dcmFile);
            ImgCase newCase = Json2Object(jsonFile);
            //deleteFile(jsonFile);
            newCase = generateImgCenter(dcmFile, newCase);
            System.out.println(getClass().getSimpleName() + " newCase: " + newCase);

            if (null == uploadCase) {
                uploadCase = new UploadCase();
                uploadCase.setBatch(assembledBatch.getBatch());
                uploadCase.getData().add(newCase);
                return uploadCase;
            }

            List<ImgCase> imgCases = uploadCase.getData();
            //System.out.println(getClass().getSimpleName() + " run() uploadCase.getData(): " + imgCases.size());
            ImgCase aCase = null;
            boolean add = false;
            for (int i = 0; i < imgCases.size(); i++) {
                aCase = imgCases.get(i);
                if (aCase.getPatientId().equals(newCase.getPatientId())) {
                    //病例下无影像数据则直接添加ImgCenter对象
                    if (0 == aCase.getImgCount()) {
                        aCase.setFields(newCase);
                    } else if (aCase.equals(newCase)) {
                        //病例下有影像数据判断是否是同一病例,是同一病例则imgCount+1,并添加ImgCenter对象
                        aCase.setImgCount(aCase.getImgCount() + 1);
                        List<ImgCenter> newCaseImgCenters = newCase.getImgCenters();
                        aCase.getImgCenters().addAll(newCaseImgCenters);
                    }
                    break;
                } else {
                    if (i == (imgCases.size() - 1)) {
                        add = true;
                    }
                }
            }
            if (add) {
                imgCases.add(newCase);
            }
            uploadCase.setData(imgCases);
            return uploadCase;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void deleteFile(File jsonFile) {
        if (jsonFile.exists()) {
            jsonFile.delete();
            System.out.println(getClass().getSimpleName() + " deleteFile...");
        }
    }

    private ImgCase generateImgCenter(File dcmFile, ImgCase newCase) {
        ArrayList<ImgCenter> imgCenters = new ArrayList<>();
        ImgCenter imgCenter = new ImgCenter();
        //设置图像地址
        String imgUrl = ImgCenterTask.getImgUrl(dcmFile);
        if (null != imgUrl) {
            imgCenter.setImgUrl(imgUrl);
            //设置1024图像地址
            String img1024Url = ImgCenterTask.getImg1024Url(new File(imgUrl));
            imgCenter.setImg1024Url(img1024Url);
            //设置缩略图地址
            String imgUrlThumb = ImgCenterTask.getImgUrlThumb(new File(imgUrl));
            imgCenter.setImgUrlThumb(imgUrlThumb);
        }

        //设置MD5值
        String md5 = MD5Utils.getMD5(dcmFile);
        imgCenter.setImgMd5(md5);
        //设置批次号
        imgCenter.setBatchNo(assembledBatch.getBatch().getBatchNo());

        imgCenters.add(imgCenter);
        newCase.setImgCenters(imgCenters);
        newCase.setImgCount(1);
        return newCase;
    }

    private File dicom2Json(File dcmFile) {
        try {
            DicomInputStream dis = new DicomInputStream(dcmFile);
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            Map<String, ?> conf = new HashMap(2);
            String jsonFilePath = dcmFile.getAbsolutePath() + ".json";
            //System.out.println("UploadCaseTask dicom2Json jsonFilePath is: " + jsonFilePath);
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
        ImgCase imgCase = new ImgCase();
        try {
            FileReader reader = new FileReader(jsonFile);
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            Map<String, Map<String, Object>> fromJson = new Gson().fromJson(reader, type);
            System.out.println(fromJson.size());
            a:
            for (Map.Entry<String, Map<String, Object>> entry : fromJson.entrySet()) {
                String key = entry.getKey();
                //System.out.println("Json2Object key is: " + key);
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
                    case DicomTag.AGE:
                        String age = getValue(entry);
                        imgCase.setAge(age);
                        break;
                    case DicomTag.SERIAL_NUMBER:
                        String serialNumber = getValue(entry);
                        Pattern pattern = Pattern.compile("\\d+");
                        Matcher matcher = pattern.matcher(serialNumber);
                        if (matcher.find()) {
                            serialNumber = matcher.group(0);
                        }
                        System.out.println(getClass().getSimpleName() + "DicomTag.SERIAL_NUMBER: " + serialNumber);
                        imgCase.setSerialNumber(Integer.parseInt(serialNumber));
                        break a;
                }
            }
            imgCase.setImgCount(1);
            imgCase.setBatchNo(assembledBatch.getBatch().getBatchNo());
            imgCase.setSource(assembledBatch.getSource());
            imgCase.setType(assembledBatch.getType());
            imgCase.setPart(assembledBatch.getPart());

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgCase;
    }

    private String getValue(Map.Entry<String, Map<String, Object>> entry) {
        Map<String, Object> tagValue = entry.getValue();
        List values = (List) tagValue.get("Value");
        String value = values.get(0).toString();
        return value;
    }

    public UploadCaseTask(File dcmFile, UploadCase uploadCase, AssembledBatch assembledBatch) {
        this.dcmFile = dcmFile;
        this.uploadCase = uploadCase;
        this.assembledBatch = assembledBatch;
    }
}

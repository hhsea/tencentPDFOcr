import com.google.gson.JsonArray;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.GeneralBasicOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.GeneralBasicOCRResponse;
import com.tencentcloudapi.ocr.v20181119.models.TextDetection;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * @author hhsea
 * @create 2021-08-10 10:58
 */
public class Application {
    static String rootPath="";
    static String fileNameTemplate="";
    static String appId="";
    static String secretId="";
    static String secretKey="";
    static int pageNum;

    public static void readCfg(){
        ResourceBundle resource = ResourceBundle.getBundle("appCfg");
        appId=resource.getString("appId");
        secretId=resource.getString("secretId");
        secretKey=resource.getString("secretKey");
        rootPath=resource.getString("rootPath");
        fileNameTemplate=resource.getString("fileNameTemplate");
        pageNum=Integer.parseInt(resource.getString("pageNum"));
    }

    public static void main(String[] args) {
        readCfg();
        String filePathTemplate=getFilePath(fileNameTemplate);
        try {
            Credential cred = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ocr.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            OcrClient client = new OcrClient(cred, "ap-shanghai", clientProfile);
            GeneralBasicOCRResponse resp =null;
            GeneralBasicOCRRequest req = new GeneralBasicOCRRequest();
            //识别PDF时，此项必须为true
            req.setIsPdf(true);
            for(int i=1;i<=pageNum;i++){
                try{
                    req.setImageBase64(encryptToBase64(String.format(filePathTemplate,i)));
                }catch (Exception e){
                    System.out.println(String.format("找不到要识别的文件：%s",String.format(filePathTemplate,i)));
                    continue;
                }
                resp = client.GeneralBasicOCR(req);
                System.out.println(GeneralBasicOCRResponse.toJsonString(resp));
                for (TextDetection item:resp.getTextDetections()){
                    System.out.println(item.getDetectedText());
                }
                saveFileTxt(GeneralBasicOCRResponse.toJsonString(resp));
            }
        }catch (TencentCloudSDKException e){
            System.out.println(e.getMessage());
            System.out.println(e);
        }catch (Exception e){
            System.out.println(e.getMessage());;
            System.out.println(e);
        }
    }

    public static String getFilePath(String fileName){
        return String.format("%s%s",rootPath,fileName);
    }

    public static String readFileTxt(){
        File file = new File("D:\\project\\java\\tencent-ocr\\src\\main\\java\\base64.txt");
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sb.append(tempStr);
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return "";
    }

    public static void saveFileTxt(String txt){
        String _fileName="\\ocrTxt.json";
        String filePath=getFilePath(_fileName);
        File f = new File(filePath);
        try{
            FileOutputStream fos = new FileOutputStream(f,true);
            OutputStreamWriter outw = new OutputStreamWriter(fos, "UTF-8");
            outw.append(txt);
            outw.close();
            fos.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
            System.out.println(e);
        }
    }

    /**
     * 文件转Base64
     * @param filePath
     * @return
     */
    public static String encryptToBase64(String filePath) {
        if (filePath == null) {
            return null;
        }
        try {
            byte[] b = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

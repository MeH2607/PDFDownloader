import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import org.example.entities.ExcelRow;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.InputStream;

public class ControllerTest {

    @Test
    void TestDownloadPDFOnFunctionalLink() throws Exception{
        ExcelRow er = new ExcelRow(2, "https://danskebank.com/-/media/danske-bank-com/file-cloud/2025/2/danske-bank---annual-report-2024.pdf?rev=430be65be4cd43d18fc8adeec2139eb5",null);

        //lets us send HTTP GET requests to target url
        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpGet httpGet = new HttpGet(er.getFileLink());
            httpClient.execute(httpGet, classicHttpResponse  -> {
                int code = classicHttpResponse.getCode();
                if(code == 200){
                    HttpEntity entity = classicHttpResponse.getEntity();
                    if(entity != null){
                        try (InputStream inputStream = entity.getContent();
                             FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(er.getFileName()))){
                                 byte[] dataBuffer = new byte[1024];
                                 int bytesRead;
                                 while((bytesRead = inputStream.read(dataBuffer))!=-1){
                                     fileOutputStream.write(dataBuffer, 0, bytesRead);
                                 }
                                 }
                    }
                    EntityUtils.consume(entity);
                }
                return classicHttpResponse;
            });
        }

    }
}

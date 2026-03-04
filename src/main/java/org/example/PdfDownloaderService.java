package org.example;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.entities.DownloadStatus;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Service
public class PdfDownloaderService {


    DownloadStatus downloadFile(String url, Path filePath) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            return httpClient.execute(httpGet, response -> {
                int code = response.getCode();
                boolean downloaded = false;

                if (code == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        try (InputStream inputStream = entity.getContent();
                             FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {

                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            downloaded = true;
                        }
                    }
                    EntityUtils.consume(entity);
                }

                return new DownloadStatus(
                        filePath.getFileName().toString(),
                        filePath.toAbsolutePath().toString(),
                        downloaded
                );
            });
        }
    }
}

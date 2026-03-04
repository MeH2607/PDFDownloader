package org.example;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.util.Timeout;
import org.example.entities.DownloadStatus;
import org.example.entities.ExcelRow;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfDownloaderService {

    private final CloseableHttpClient httpClient;

    public PdfDownloaderService() {

        PoolingHttpClientConnectionManager cm =
                new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(50);
        cm.setDefaultMaxPerRoute(10);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofSeconds(60))
                .build();

        this.httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public DownloadStatus downloadFile(ExcelRow er) throws IOException {
        String userHome = System.getProperty("user.home");
        Path folderPath = Paths.get(userHome, "Downloads", "Reports");
        Files.createDirectories(folderPath);

        // Extract original filename from URL safely
        String originalFileName = Paths.get(new URL(er.getFileLink()).getPath())
                .getFileName().toString();
        String decodedOriginal = URLDecoder.decode(originalFileName, StandardCharsets.UTF_8);

        // Build new filename with ID prefix
        String newFileName = er.getFileName() + " - " + decodedOriginal;
        Path filePath = folderPath.resolve(newFileName);


        try {
            HttpGet httpGet = new HttpGet(er.getFileLink());

            // 🔥 Critical: Add browser-like headers
            httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36");

            httpGet.setHeader("Accept",
                    "application/pdf,application/octet-stream;q=0.9,*/*;q=0.8");

            return httpClient.execute(httpGet, response -> {

                if (response.getCode() != 200) {
                    return new DownloadStatus(
                            filePath.getFileName().toString(),
                            null,
                            false
                    );
                }

                HttpEntity entity = response.getEntity();

                try (InputStream inputStream = entity.getContent();
                     FileOutputStream outputStream =
                             new FileOutputStream(filePath.toFile())) {

                    inputStream.transferTo(outputStream);
                }

                return new DownloadStatus(
                        filePath.getFileName().toString(),
                        filePath.toAbsolutePath().toString(),
                        true
                );
            });

        } catch (Exception e) {

            return new DownloadStatus(
                    filePath.getFileName().toString(),
                    null,
                    false
            );
        }
    }
}
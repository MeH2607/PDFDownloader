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
import org.example.excel.ApachePoiExcelReader;
import org.example.excel.ApachePoiExcelWriter;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.apache.commons.io.file.PathUtils.deleteDirectory;

@Service
public class PdfDownloaderService {

    private final CloseableHttpClient httpClient;
    private final ApachePoiExcelReader apachePoiExcelReader;
    private final Path reportsFolder;
    private final ApachePoiExcelWriter apachePoiExcelWriter;
    private final ExecutorService executor = Executors.newFixedThreadPool(10); //For async download



    public PdfDownloaderService(ApachePoiExcelReader apachePoiExcelReader, ApachePoiExcelWriter apachePoiExcelWriter) throws IOException {
        this.apachePoiExcelReader = apachePoiExcelReader;

        String userHome = System.getProperty("user.home");
        this.reportsFolder = Paths.get(userHome, "Downloads", "Reports", "downloaded_files");

        Files.createDirectories(reportsFolder);


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
        this.apachePoiExcelWriter = apachePoiExcelWriter;
    }


    public List<DownloadStatus> downloadPdfsFromExcelFile(String excelInput) throws Exception {


        List<DownloadStatus> downloadStatusList = new ArrayList<>();
        List<ExcelRow> rows = apachePoiExcelReader.read(excelInput);



        //Parallel download
        List<Future<DownloadStatus>> futures = new ArrayList<>();

        for (ExcelRow er : rows) {
            futures.add(executor.submit(() -> downloadFile(er)));
        }

        for (Future<DownloadStatus> future : futures) {
            downloadStatusList.add(future.get());
        }


        //Adds the Report folder as parameter
        apachePoiExcelWriter.write(downloadStatusList, reportsFolder.getParent().toString());

        return downloadStatusList;
    }

    public DownloadStatus downloadFile(ExcelRow er) {

        List<String> downloadLinks = new ArrayList<>();

        downloadLinks.add(er.getFileLink());

        if (er.getBackupLink() != null && !er.getBackupLink().isBlank()) {
            downloadLinks.add(er.getBackupLink());
        }

        for (String link : downloadLinks) {

            DownloadStatus status = attemptDownload(link, er);

            if (status.isDownloaded()) {
                return status;
            }
        }

        return new DownloadStatus(
                er.getFileName() + " - failed to download",
                null,
                false
        );
    }

    private DownloadStatus attemptDownload(String link, ExcelRow er) {

        try {

            if (link == null || link.isBlank()) {
                return new DownloadStatus(er.getFileName() + " - missing URL", null, false);
            }

            URL url = new URL(link);

            String originalFileName = Paths.get(url.getPath())
                    .getFileName()
                    .toString();

            String decodedOriginal =
                    URLDecoder.decode(originalFileName, StandardCharsets.UTF_8);

            String newFileName =
                    er.getFileName() + " - " + decodedOriginal;

            Path filePath = reportsFolder.resolve(newFileName);

            HttpGet httpGet = new HttpGet(link);

            httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            httpGet.setHeader("Accept",
                    "application/pdf,application/octet-stream;q=0.9,*/*;q=0.8");

            return httpClient.execute(httpGet, response -> {

                if (response.getCode() != 200) {
                    return new DownloadStatus(newFileName, null, false);
                }

                HttpEntity entity = response.getEntity();

                try (InputStream inputStream = entity.getContent();
                     FileOutputStream outputStream =
                             new FileOutputStream(filePath.toFile())) {

                    inputStream.transferTo(outputStream);
                }

                return new DownloadStatus(
                        newFileName,
                        filePath.toAbsolutePath().toString(),
                        true
                );
            });

        } catch (Exception e) {
            return new DownloadStatus(er.getFileName() + " - error", null, false);
        }
    }

    //TODO fix parent folder and report file not deleting. Add to frontend
    public void deleteFile() throws IOException{
        Files.walk(reportsFolder)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}


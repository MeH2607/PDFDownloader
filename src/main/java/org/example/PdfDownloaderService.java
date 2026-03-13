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
import java.util.concurrent.TimeUnit;

import static org.apache.commons.io.file.PathUtils.deleteDirectory;

@Service
/**
 * Serviceclass for the application where logic is being handled
 */
public class PdfDownloaderService {

    /**
     *  handles the http calls
     */
    private final CloseableHttpClient httpClient;
    /**
     * the filepath where the pdf's and the report file will be saved
     * Will always be saved under user/username/downloads/Reports
     */
    private final Path reportsFolder;
    private final ApachePoiExcelReader apachePoiExcelReader;
    private final ApachePoiExcelWriter apachePoiExcelWriter;




    public PdfDownloaderService(ApachePoiExcelReader apachePoiExcelReader, ApachePoiExcelWriter apachePoiExcelWriter) throws IOException {
        this.apachePoiExcelReader = apachePoiExcelReader;

        String userHome = System.getProperty("user.home");
        this.reportsFolder = Paths.get(userHome, "Downloads", "Reports");

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


    /**
     * Reads every row of the the provided excel sheet and calls the downloadFiles method to download and save the files.
     * The method is able to handle the rows parallel thanks to the ExecutorService api
     * also handles writing and saving the result of all downloads to an excel file
     * @param excelInput the excelFile recieved from the frontend
     * @return a list of the status of all the downloads
     */
    public List<DownloadStatus> downloadPdfsFromExcelFile(String excelInput) throws Exception {


        List<DownloadStatus> downloadStatusList = new ArrayList<>();
        List<ExcelRow> rows = apachePoiExcelReader.read(excelInput);


        //Parallel download
        List<Future<DownloadStatus>> futures = new ArrayList<>();

        /**
         * The API that lets us handle pdf downloads parallel.
         */
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            for (ExcelRow er : rows) {
                futures.add(executor.submit(() -> downloadFile(er)));
            }

            for (Future<DownloadStatus> future : futures) {
                downloadStatusList.add(future.get());
            }

        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.MINUTES);
        }


        /**
         *Adds the parent Report folder as parameter
         */
        apachePoiExcelWriter.write(downloadStatusList, reportsFolder.toString());

        return downloadStatusList;
    }

    /**

     * Calls the AttempDownload method for every row, and will try again with the backup link on failure if applicable
     * @param er every excel row and handles download success and failures
     * @return the download status recieved from @attemptDownload()
     */
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

    /**
     * Handles the actual download of the pdfs.
     * @param link the url attempted to be downloaded
     * @param er the whole row object
     * @return the download status of the attempted pdf download
     */
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

    //TODO fix parent folder and report file not deleting.
    /**
     * Deletes the files and folders that has been made.
     * Currently buggy. Deletes the subfolder but can't delete the parent folder and it's files
     */
    public void deleteFile() throws IOException{
        if (Files.exists(reportsFolder)) {
            Files.walk(reportsFolder)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Kunne ikke slette: " + path + " - " + e.getMessage());
                        }
                    });
        }

    }

}



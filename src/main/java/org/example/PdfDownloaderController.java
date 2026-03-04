package org.example;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.entities.DownloadStatus;
import org.example.entities.ExcelRow;
import org.example.excel.ApachePoiExcelReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor

@RestController
public class PdfDownloaderController {

    private final PdfDownloaderService pdfDownloaderService;
    private final ApachePoiExcelReader apachePoiExcelReader;


    public List<DownloadStatus> downloadPdfs(String excelInput) throws Exception {
        String userHome = System.getProperty("user.home");
        Path folderPath = Paths.get(userHome, "Downloads", "Reports");
        Files.createDirectories(folderPath);

        List<DownloadStatus> downloadStatusList = new ArrayList<>();
        List<ExcelRow> rows = apachePoiExcelReader.read(excelInput);

        for (ExcelRow er : rows) {
            // Extract original filename from URL safely
            String originalFileName = Paths.get(new URL(er.getFileLink()).getPath())
                    .getFileName().toString();
            String decodedOriginal = URLDecoder.decode(originalFileName, StandardCharsets.UTF_8);

            // Build new filename with ID prefix
            String newFileName = er.getFileName() + " - " + decodedOriginal;
            Path filePath = folderPath.resolve(newFileName);

            DownloadStatus downloadStatus;

            try {
                // Try the original link first
                downloadStatus = pdfDownloaderService.downloadFile(er.getFileLink(), filePath);

                // If download failed, try the backup link
                if (!downloadStatus.isDownloaded() && er.getBackupLink() != null) {
                    downloadStatus = pdfDownloaderService.downloadFile(er.getBackupLink(), filePath);
                }
                if (!downloadStatus.isDownloaded()) {
                    downloadStatus = new DownloadStatus(String.valueOf(er.getFileName() + " - failed to download"), null, false);
                }

                downloadStatusList.add(downloadStatus);
            } catch (IOException e) {
                // Handle exceptions (optional: log and fail test)
                throw e;
            }
        }
        return downloadStatusList;
    }

    @PostMapping("/pdf/test-from-local-excel")
    public ResponseEntity<List<DownloadStatus>> testFromLocalExcel() throws Exception {
        String excelPath = "src/GRI_2017_2025.xlsx";
        return ResponseEntity.ok(downloadPdfs(excelPath));
    }

}

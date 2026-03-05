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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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



    @PostMapping("/pdf/test-from-local-excel")
    public ResponseEntity<List<DownloadStatus>> testFromLocalExcel() throws Exception {
        String excelPath = "src/GRI_2017_2025_test.xlsx";
        return ResponseEntity.ok(pdfDownloaderService.downloadPdfs(excelPath));
    }

    @PostMapping(value = "/pdf/upload-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DownloadStatus>> uploadExcel(@RequestParam("file") MultipartFile  file) throws Exception {
        Path temp = Files.createTempFile("upload-", ".xlsx");
        file.transferTo(temp.toFile());
        try {
            return ResponseEntity.ok(pdfDownloaderService.downloadPdfs(temp.toString()));
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    @GetMapping("/pdf/getStatusList")
    public ResponseEntity<List<DownloadStatus>> getAllDownloadStatuses(List<DownloadStatus> downloadStatusList) {

        return ResponseEntity.ok(downloadStatusList);
    }
}
package org.example;

import lombok.AllArgsConstructor;
import org.example.entities.DownloadStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AllArgsConstructor

@RestController
@RequestMapping("/pdf/")
public class PdfDownloaderController {

    private final PdfDownloaderService pdfDownloaderService;



    @PostMapping("test-from-local-excel")
    public ResponseEntity<List<DownloadStatus>> testFromLocalExcel() throws Exception {
        String excelPath = "src/GRI_2017_2025_test - 50 entries.xlsx";
        return ResponseEntity.ok(pdfDownloaderService.downloadPdfsFromExcelFile(excelPath));
    }

    @PostMapping(value = "pdf/upload-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DownloadStatus>> uploadExcel(@RequestParam("file") MultipartFile  file) throws Exception {
        Path temp = Files.createTempFile("upload-", ".xlsx");
        file.transferTo(temp.toFile());
        try {
            return ResponseEntity.ok(pdfDownloaderService.downloadPdfsFromExcelFile(temp.toString()));
        } finally {
            Files.deleteIfExists(temp);
        }


    }

    @DeleteMapping("delete-path")
    public ResponseEntity deletePath() throws Exception{

        pdfDownloaderService.deleteFile();
        return ResponseEntity.ok("All files and folder has been deleted.");
    }



}
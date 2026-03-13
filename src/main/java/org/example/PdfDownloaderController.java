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
/**
 * The controller class handling Rest api calls
 */
public class PdfDownloaderController {

    private final PdfDownloaderService pdfDownloaderService;


    /**
     * Test method for testing a local excel sheet
     * made for quick tests with postman
     */
    @PostMapping("test-from-local-excel")
    public ResponseEntity<List<DownloadStatus>> testFromLocalExcel() throws Exception {
        String excelPath = "src/GRI_2017_2025_test - 50 entries.xlsx";
        return ResponseEntity.ok(pdfDownloaderService.downloadPdfsFromExcelFile(excelPath));
    }

    /**
     * Post method to upload the excel file that is being sent to service
     * @param file MultipartFile is the way Java handles these file upload best, and gets transformed to a temp file that ApachePOI can work with
     * @return ResponseEntity with the list of download statuses.
     */
    @PostMapping(value = "upload-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DownloadStatus>> uploadExcel(@RequestParam("file") MultipartFile  file) throws Exception {
        Path temp = Files.createTempFile("upload-", ".xlsx");
        file.transferTo(temp.toFile());
        try {
            return ResponseEntity.ok(pdfDownloaderService.downloadPdfsFromExcelFile(temp.toString()));
        } finally {
            Files.deleteIfExists(temp);
        }


    }

    /**
     * Deletes the downloaded pdf files, and the directory
     * Should also delete the parent folder and the excel file
     * Service method is buggy right now.
     */
    @DeleteMapping("delete-path")
    public ResponseEntity deletePath() throws Exception{

        pdfDownloaderService.deleteFile();
        return ResponseEntity.ok("All files and folder has been deleted.");
    }



}
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import org.example.entities.DownloadStatus;
import org.example.entities.ExcelRow;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ControllerTest {

    @Test
    void testDownloadPDFOnFunctionalLink() throws Exception {
        // -------------------------------
        // Arrange: set up ExcelRow and folder
        // -------------------------------
        ExcelRow er = new ExcelRow(
                2,
                "https://danskebank.com/-/media/danske-bank-com/file-cloud/2025/2/danske-bank---annual-report-2024.pdf?rev=430be65be4cd43d18fc8adeec2139eb5",
                null
        );

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

        DownloadStatus downloadStatus;

        // -------------------------------
        // Act: download the file
        // -------------------------------
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(er.getFileLink());

            downloadStatus = httpClient.execute(httpGet, response -> {
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

        // -------------------------------
        // Assert: verify file and filename
        // -------------------------------
        assertEquals("2 - danske-bank---annual-report-2024.pdf", downloadStatus.getFileName());
        assertTrue(downloadStatus.isDownloaded());
        assertTrue(Files.exists(Paths.get(downloadStatus.getFilePath())));
    }

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

    @Test
    void testDownloadPDFOnBackupLink() throws Exception {

        ExcelRow er = new ExcelRow(1,
                "https://www.novonordisk.com/content/dam/nncorp/global/en/investors/irmaterial/annual_report/2025/novo-nordisk-annual-report-2024.pdfst",
                "https://www.novonordisk.com/content/dam/nncorp/global/en/investors/irmaterial/annual_report/2025/novo-nordisk-annual-report-2024.pdf");

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

        DownloadStatus downloadStatus;

        try {
            // Try the original link first
            downloadStatus = downloadFile(er.getFileLink(), filePath);

            // If download failed, try the backup link
            if (!downloadStatus.isDownloaded()) {
                downloadStatus = downloadFile(er.getBackupLink(), filePath);
            }
            if (!downloadStatus.isDownloaded()) {
                downloadStatus = new DownloadStatus(String.valueOf(er.getFileName() + " - failed to download"), null, false);
            }
        } catch (IOException e) {
            // Handle exceptions (optional: log and fail test)
            throw e;
        }

        // -------------------------------
        // Assert: verify file and filename
        // -------------------------------
        assertEquals("1 - novo-nordisk-annual-report-2024.pdfst", downloadStatus.getFileName());
        assertTrue(downloadStatus.isDownloaded());
        assertTrue(Files.exists(Paths.get(downloadStatus.getFilePath())));
    }

    @Test
    void testDownloadPDFFail() throws Exception {

        ExcelRow er = new ExcelRow(3, "https://www.cbs.dk/files/cbs.dk/call_to_action/cbs-annual-report-2024.pdf", null);


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

        DownloadStatus downloadStatus;

        try {
            // Try the original link first
            downloadStatus = downloadFile(er.getFileLink(), filePath);

            // If download failed, try the backup link
            if (!downloadStatus.isDownloaded() && er.getBackupLink() != null) {
                downloadStatus = downloadFile(er.getBackupLink(), filePath);
            }
            if (!downloadStatus.isDownloaded()) {
                downloadStatus = new DownloadStatus(String.valueOf(er.getFileName() + " - failed to download"), null, false);
            }
        } catch (IOException e) {
            // Handle exceptions (optional: log and fail test)
            throw e;
        }

        // -------------------------------
        // Assert: verify file and filename
        // -------------------------------
        assertEquals("3 - failed to download", downloadStatus.getFileName());
        assertTrue(!downloadStatus.isDownloaded());;
    }


}


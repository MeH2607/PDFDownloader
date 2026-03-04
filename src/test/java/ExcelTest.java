import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entities.DownloadStatus;
import org.example.entities.ExcelRow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ExcelTest {

    ExcelRow er0 = new ExcelRow(1, "https://www.novonordisk.com/content/dam/nncorp/global/en/investors/irmaterial/annual_report/2025/novo-nordisk-annual-report-2024.pdfst","https://www.novonordisk.com/content/dam/nncorp/global/en/investors/irmaterial/annual_report/2025/novo-nordisk-annual-report-2024.pdf");
    ExcelRow er1 = new ExcelRow(2, "https://danskebank.com/-/media/danske-bank-com/file-cloud/2025/2/danske-bank---annual-report-2024.pdf?rev=430be65be4cd43d18fc8adeec2139eb5",null);
    ExcelRow er2 = new ExcelRow(3, "https://www.cbs.dk/files/cbs.dk/call_to_action/cbs-annual-report-2024.pdf", null);
    ExcelRow er3 = new ExcelRow(4, "https://www.nordea.com/en/doc/annual-report-nordea-bank-abp-2024-0.pdf",null);
    ExcelRow er4 = new ExcelRow(5, "https://www.eifo.dk/media/f23eicl4/eifo-annual-report-2026.pdf","https://www.eifo.dk/media/f23eicl4/eifo-annual-report-2024.pdf");
    ArrayList<ExcelRow> expectedRows = new ArrayList<>(Arrays.asList(er0, er1, er2, er3,er4));
    @Test
    void readFirst5LinesOfFile(){


        String fileLocation = "src/GRI_2017_2025.xlsx";
        FileInputStream file;
        Workbook workbook;
        List<ExcelRow> files = new ArrayList<>();
        try{
         file = new FileInputStream(new File(fileLocation));
         workbook = new XSSFWorkbook(file);

         Sheet sheet = workbook.getSheetAt(0);


        int rowCount = 0; //to be able to control how many rows I wanna test

            boolean isFirstRow = true;

            for (Row row : sheet) {

                if(rowCount == 5){
                    break;
                }

                if (isFirstRow) {
                    isFirstRow = false;
                    continue;}

             ExcelRow excelRow = new ExcelRow();
             int coloumn = 0;

            for(Cell cell : row){
                switch (coloumn){
                    case 0:
                        excelRow.setFileName((int)cell.getNumericCellValue());
                        break;
                    case 1:
                        excelRow.setFileLink(cell.getStringCellValue());
                        break;
                    case 2:
                        if(cell.getStringCellValue().equals("")){
                            excelRow.setBackupLink(null);
                        }
                        else{
                        excelRow.setBackupLink(cell.getStringCellValue());
                        }
                        break;
                }

                coloumn++;
            }
            files.add(excelRow);
            rowCount++;
         }

        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }

        assertEquals(5, files.size());

        for(int i = 0; i<expectedRows.size(); i++){
            ExcelRow expectedRow = expectedRows.get(i);
            ExcelRow actualRow = files.get(i);

            assertEquals(expectedRow.getFileName(), actualRow.getFileName(), "FileName mismatch at index " + i);
            assertEquals(expectedRow.getFileLink(), actualRow.getFileLink(), "FileLink mismatch at index " + i);
            assertEquals(expectedRow.getBackupLink(), actualRow.getBackupLink(), "BackupLink mismatch at index " + i);
        }

    }

    @Test
    void testWriteToExcelFile() throws IOException{ //TODO fix  formatting



        DownloadStatus downloadStatus1 = new DownloadStatus("1 - file", "Path/path/report", true);
        DownloadStatus downloadStatus2 = new DownloadStatus("2 - failed to download", null, false);
        DownloadStatus downloadStatus3 = new DownloadStatus("3 - file", "Path/path/report", true);
        DownloadStatus downloadStatus4 = new DownloadStatus("4 - failed to download", null, false);
        DownloadStatus downloadStatus5 = new DownloadStatus("5 - file", "Path/path/report", true);

        ArrayList<DownloadStatus> downloads = new ArrayList<>(Arrays.asList(downloadStatus1,downloadStatus2,downloadStatus3,downloadStatus4,downloadStatus5));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Persons");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4500);
        sheet.setColumnWidth(2, 6200);

        Row header = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THICK);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Filename");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(1);
        headerCell.setCellValue("Filepath");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(2);
        headerCell.setCellValue("Download status");
        headerCell.setCellStyle(headerStyle);

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        for(int i = 0; i<downloads.size(); i++){
            Row row = sheet.createRow(i+1); //create row in row 2 and up

            Cell cell = row.createCell(0);
            cell.setCellValue(downloads.get(i).getFileName());
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue(downloads.get(i).getFilePath());
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue(downloads.get(i).isDownloaded() ? "Download successful":"Download failed");
            cell.setCellStyle(style);
        }

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "writeTest.xlsx";

        // ✅ Delete file if it already exists (for clean test run)
        Path filePath = Paths.get(fileLocation);
        Files.deleteIfExists(filePath);


        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();

        File file = new File(fileLocation);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
}

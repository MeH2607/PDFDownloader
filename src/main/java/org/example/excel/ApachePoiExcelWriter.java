package org.example.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entities.DownloadStatus;
import org.example.entities.ExcelRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
/**
 * The api used for writing to excel files
 */
public class ApachePoiExcelWriter implements ExcelWriter {
    public void write(List<DownloadStatus> downloadList, String downloadFilePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PDF Downloads");
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

        for(int i = 0; i<downloadList.size(); i++){
            Row row = sheet.createRow(i+1); //create row in row 2 and up

            Cell cell = row.createCell(0);
            cell.setCellValue(downloadList.get(i).getFileName());
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue(downloadList.get(i).getFilePath());
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue(downloadList.get(i).isDownloaded() ? "Download successful":"Download failed");
            cell.setCellStyle(style);
        }



        String fileLocation = Paths.get(downloadFilePath, "000 - Download_Report.xlsx").toString();


        try (FileOutputStream outputStream = new FileOutputStream(fileLocation)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}

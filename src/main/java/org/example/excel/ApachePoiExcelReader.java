package org.example.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entities.ExcelRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
/**
 * The api used for reading the excel files
 */
public class ApachePoiExcelReader implements ExcelReader {
    public List<ExcelRow> read(String filePath) {

        FileInputStream file;
        Workbook workbook;
        List<ExcelRow> files = new ArrayList<>(); //TODO rename
        try{
            file = new FileInputStream(new File(filePath));
            workbook = new XSSFWorkbook(file);

            Sheet sheet = workbook.getSheetAt(0);


            boolean isFirstRow = true;

            for (Row row : sheet) {



                if (isFirstRow) {
                    isFirstRow = false;
                    continue;}

                if(isRowEmpty(row)){
                    break;
                }

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

            }

        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        return files;
    }

    private boolean isRowEmpty(Row row) {

        if (row == null) {
            return true;
        }

        for (int cellNum = row.getFirstCellNum();
             cellNum < row.getLastCellNum();
             cellNum++) {

            Cell cell = row.getCell(cellNum);

            if (cell != null &&
                    cell.getCellType() != CellType.BLANK &&
                    !cell.toString().trim().isEmpty()) {

                return false; // Found non-empty cell
            }
        }

        return true; // All cells empty
    }
}

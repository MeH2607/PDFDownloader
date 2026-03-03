import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entities.ExcelRow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
}

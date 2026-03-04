package org.example.excel;

import org.example.entities.DownloadStatus;
import org.example.entities.ExcelRow;

import java.util.List;

public class ApachePoiExcelWriter implements ExcelWriter {
    public void write(String filePath, List<DownloadStatus> downloadList) {
        // use Apache POI to write Excel
    }
}

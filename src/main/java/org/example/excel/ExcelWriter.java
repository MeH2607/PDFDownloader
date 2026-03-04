package org.example.excel;

import org.example.entities.DownloadStatus;
import org.example.entities.ExcelRow;

import java.util.List;

public interface ExcelWriter {
    void write(List<DownloadStatus> downloadList);
}

package org.example.excel;

import org.example.entities.ExcelRow;

import java.util.List;

public interface ExcelWriter {
    void write(String filePath, List<ExcelRow> rows);
}

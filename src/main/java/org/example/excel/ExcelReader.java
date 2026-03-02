package org.example.excel;

import org.example.entities.ExcelRow;

import java.util.List;

public interface ExcelReader {
    List<ExcelRow> read(String filePath);
}
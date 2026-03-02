package org.example.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class ExcelRow {

    int fileName;
    String fileLink;
    String backupLink;
    boolean isDownloaded;

    public ExcelRow() {
        this.isDownloaded = false;
    }
}

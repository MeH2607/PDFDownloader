package org.example.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExcelRow {

    int fileName;
    String fileLink;
    String backupLink;
    boolean isDownloaded;

    public void setFileName(double fileName) {
        this.fileName = (int)fileName;
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }


}

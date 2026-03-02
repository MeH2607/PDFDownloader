package org.example.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExcelRow {

    String fileName;
    String fileLink;
    boolean isDownloaded;
}

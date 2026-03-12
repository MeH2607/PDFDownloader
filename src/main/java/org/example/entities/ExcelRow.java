package org.example.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * Represents the individual row in the excel sheet. Includes the original filename, the primary file link and the backup link.
 */
public class ExcelRow {

    int fileName;
    String fileLink;
    String backupLink;


}

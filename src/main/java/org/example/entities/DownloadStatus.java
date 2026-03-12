package org.example.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
/**
 * The DTO for the status of a pdf download. Includes the newly generated filename,
 * local filepath and a boolean on if the download was succesful or not
 */
public class DownloadStatus {
    private String fileName;
    private String filePath;
    private boolean isDownloaded;
}

package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.entities.DownloadStatus;

import java.util.List;


public record DownloadStatusRecord(String filepath, List<DownloadStatus> downloadStatusList) {

}

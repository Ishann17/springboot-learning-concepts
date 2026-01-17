package com.ishan.user_service.dto;

import lombok.Data;

@Data
public class ImportJobStatusDto {

    String jobId;
    String status;
    int requestedCount;
    long processedCount;
    String message;
    long startedAt;
    long finishedAt;
}

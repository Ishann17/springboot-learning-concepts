package com.ishan.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the status of a long-running user import job.
 * Why this DTO exists:
 * - Import runs asynchronously (not tied to HTTP request)
 * - Client gets a jobId and polls status later
 * - Keeps API responsive for heavy operations (millions of records)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportUserJobInfoDto {

    String jobId;
    ImportStatus status;
    long requestedCount;
    long processedCount;
    String message;
    long startedAt;
    long finishedAt;

    /**
     * Possible states of an import job.
     * - IN_PROGRESS: job is running
     * - COMPLETED: job finished successfully
     * - FAILED: job stopped due to error
     */
    public enum ImportStatus{
        IN_PROGRESS, COMPLETED, FAILED, PENDING
    }
}



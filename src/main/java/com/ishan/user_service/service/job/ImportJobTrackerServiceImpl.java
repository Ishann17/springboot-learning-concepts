package com.ishan.user_service.service.job;

import com.ishan.user_service.dto.ImportJobStatusDto;
import org.springframework.stereotype.Service;

@Service
public class ImportJobTrackerServiceImpl implements ImportJobTrackerService{

    @Override
    public String createJob(int requestedCount) {
        return "";
    }

    @Override
    public ImportJobStatusDto getJobStatus(String jobId) {
        return null;
    }

    @Override
    public void markRunning(String jobId) {

    }

    @Override
    public void updateProgress(String jobId, long processedCount) {

    }

    @Override
    public void markCompleted(String jobId, String message) {

    }

    @Override
    public void markFailed(String jobId, String errorMessage) {

    }
}

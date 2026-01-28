package com.ishan.user_service.controller;

import com.ishan.user_service.dto.ImportUserJobInfoDto;
import com.ishan.user_service.service.job.ImportUserJobTrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/jobs")
public class ImportUserJobController {
    @Autowired
    private ImportUserJobTrackerService importUserJobTrackerService;

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJobStatus(@PathVariable String jobId) {

        ImportUserJobInfoDto jobInfo =
                importUserJobTrackerService.getJobStatus(jobId);

        if (jobInfo == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Job not found"));
        }

        return ResponseEntity.ok(jobInfo);
    }
}

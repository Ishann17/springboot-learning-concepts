package com.ishan.user_service.controller;

import com.ishan.user_service.service.user.UserExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/v1/users")
public class UserExportController {

    private static final Logger log = LoggerFactory.getLogger(UserExportController.class);

    @Autowired
    private UserExportService userExportService;

    /**
     * ✅ File-based CSV Export (Old Approach)
     *
     * What it does:
     * - Fetches users from DB
     * - Writes a CSV file to the given "path" on disk
     * - Returns the created file as a downloadable response
     *
     * Why we still keep this endpoint:
     * - Useful for learning and revision
     * - Useful when you want the CSV stored locally on server machine
     *
     * ⚠️ Risk:
     * - If implemented using findAll(), it can spike RAM heavily for millions of rows.
     * - Streaming export is the production-friendly approach.
     */
    @GetMapping("/exportCSV")
    public ResponseEntity<?> exportAllUsersToCSV(
            @RequestParam(defaultValue = "C:\\Users\\Ishan Raghav\\Downloads") String path
    ) {

        log.info("CSV file export requested. Path={}", path);

        Resource resource = userExportService.exportAllUsersToCSV(path);

        // ✅ Safety check: If file was not generated, return clear error response
        if (resource == null || !resource.exists()) {
            log.error("CSV file export FAILED. File was not generated at path={}", path);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSV export failed. File could not be generated.");
        }

        String fileName = resource.getFilename();

        log.info("CSV file export SUCCESS. FileName={} Path={}", fileName, path);

        // ✅ SUCCESS CASE:
        // Content-Disposition triggers browser download
        // Content-Type is set to "text/csv"
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    /**
     * ✅ Streaming CSV Export (Best Approach for huge datasets)
     *
     * What it does:
     * - Does NOT create a file on disk
     * - Streams rows directly to the HTTP response output stream
     *
     * Why it is best:
     * - No RAM spike (we fetch + write in batches)
     * - No SSD usage (no big temp file needed)
     * - Works smoothly for millions of records
     *
     * Note:
     * - Postman normal "Send" may fail for huge responses due to size limits.
     * - Use Postman "Send and Download" OR test in browser.
     */
    @GetMapping("/exportCSV/stream")
    public ResponseEntity<StreamingResponseBody> exportAllUsersToCSVStream() {

        log.info("CSV streaming export requested.");

        // StreamingResponseBody means Spring will keep writing to response as data is generated
        // instead of building the entire output in memory first.
        StreamingResponseBody body = outputStream -> {
            log.info("CSV streaming started...");
            userExportService.streamUsersAsCsv(outputStream);
            log.info("CSV streaming finished.");
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"AllUsersCSV.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }
}

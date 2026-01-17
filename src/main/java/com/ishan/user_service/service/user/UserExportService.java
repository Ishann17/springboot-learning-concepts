package com.ishan.user_service.service.user;

import org.springframework.core.io.Resource;

import java.io.OutputStream;

public interface UserExportService {
    Resource exportAllUsersToCSV(String path);
    void streamUsersAsCsv(OutputStream outputStream);
}

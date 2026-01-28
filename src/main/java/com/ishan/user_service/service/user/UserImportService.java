package com.ishan.user_service.service.user;

import com.ishan.user_service.dto.UserDto;
import com.ishan.user_service.model.User;

import java.util.List;

public interface UserImportService {
    User importSingleUserFromExternalSource(UserDto userDto);
    void importMultipleUsersFromExternalSource(List<UserDto> userDtoList);
    void importMultipleUsersFromFakerWithBatchProcessing(String jobId,List<UserDto> userDtoList);
}

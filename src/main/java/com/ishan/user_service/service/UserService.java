package com.ishan.user_service.service;

import com.ishan.user_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    User createNewUser(User user);
    User getUserById(int id);
    String exportAllUsersToCSV(String path);
    Page<User> searchUserByAge(int minAge, int maxAge, Pageable pageable);
    Page<User> searchUsers(String name, Integer age, String city, String state, Pageable pageable);
}

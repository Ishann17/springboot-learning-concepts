package com.ishan.user_service.repository;

import com.ishan.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findByAgeBetween(int minAge, int maxAge);

}

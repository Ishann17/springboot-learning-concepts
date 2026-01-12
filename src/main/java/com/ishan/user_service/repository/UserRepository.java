package com.ishan.user_service.repository;

import com.ishan.user_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    //Pagination & Sorting both are done at DB Level. Sorting comes first then Pagination
    Page<User> findByAgeBetween(int minAge, int maxAge, Pageable pageable);
}

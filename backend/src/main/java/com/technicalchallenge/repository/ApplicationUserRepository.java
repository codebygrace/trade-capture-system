package com.technicalchallenge.repository;

import com.technicalchallenge.model.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
    // Custom query methods if needed
    Optional<ApplicationUser> findByLoginId(String loginId);
    Optional<ApplicationUser> findByFirstName(String firstName);

    // Eager fetch required by Spring Security when it calls getAuthorities()
    // This allows access to UserPrivilege and Privilege in an active session
    @Query("SELECT u FROM ApplicationUser u " +
            "LEFT JOIN FETCH u.userPrivileges up LEFT JOIN FETCH up.privilege " +
            "LEFT JOIN FETCH u.userProfile WHERE u.loginId = :loginId ")
    Optional<ApplicationUser> findByLoginIdWithPrivileges(@Param("loginId") String loginId);

}

package com.kt.myrestapi.security.userinfo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    Optional<UserInfo> findByEmail(String username);
    Optional<UserInfo> findByUserId(String userId);
}
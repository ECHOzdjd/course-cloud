package com.zjgsu.wy.user.repository;

import com.zjgsu.wy.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 判断用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 判断邮箱是否存在
     */
    boolean existsByEmail(String email);
}

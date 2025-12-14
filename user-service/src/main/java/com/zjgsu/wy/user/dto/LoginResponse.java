package com.zjgsu.wy.user.dto;

import com.zjgsu.wy.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserInfo user;
    
    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = new UserInfo(user);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String role;
        private String email;
        private String realName;
        
        public UserInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.role = user.getRole();
            this.email = user.getEmail();
            this.realName = user.getRealName();
        }
    }
}

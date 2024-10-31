package com.moyeobwayo.moyeobwayo.Domain.response;

import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private UserEntity user;
    private String message;
    private String token; // JWT 토큰 추가

    public UserResponse(UserEntity user, String message) {
        this.user = user;
        this.message = message;
    }

    public UserResponse(UserEntity user, String message, String token) {
        this.user = user;
        this.message = message;
        this.token = token;
    }
}
package com.moyeobwayo.moyeobwayo.Domain.response;

import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private UserEntity user;
    private String message;

    public UserResponse(UserEntity user, String message) {
        this.user = user;
        this.message = message;
    }
}
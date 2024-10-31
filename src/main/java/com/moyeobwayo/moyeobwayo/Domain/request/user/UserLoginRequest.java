package com.moyeobwayo.moyeobwayo.Domain.request.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Setter
@ToString
public class UserLoginRequest {
    private String userName;
    private String password;
    private String partyId;
    @JsonProperty("kakaoUserId")
    private Long kakaoUserId; // 그대로 유지

    @JsonProperty("isKakao")
    private boolean isKakao;

    public Optional<Long> getKakaoUserId() {
        return Optional.ofNullable(kakaoUserId);
    }
}
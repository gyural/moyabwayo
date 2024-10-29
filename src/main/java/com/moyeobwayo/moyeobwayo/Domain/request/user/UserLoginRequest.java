package com.moyeobwayo.moyeobwayo.Domain.request.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserLoginRequest {
    // DTO
    private String userName;
    private String password;
    private String partyId;  // 로그인 시 파티 ID도 함께 전달
    private String KakaoUserId;
    @JsonProperty("isKakao")
    private boolean isKakao;  // 카카오 유저인지 여부 추가

}
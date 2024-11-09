package com.moyeobwayo.moyeobwayo.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class KakaoProfile {

    @Id  // @GeneratedValue(strategy = GenerationType.IDENTITY) 삭제
    private Long kakaoUserId;  // 카카오에서 제공하는 ID를 직접 저장 (Long)

    private String profile_image;
    private String nickname;

    private String access_token;
    private String refresh_token;

    private boolean kakao_message_allow = true;
    private boolean alarm_off = true;

    private Long expires_in;  // 액세스 토큰 만료 시간 (초 단위)
    private Long refresh_token_expires_in;  // 리프레시 토큰 만료 시간 (초 단위)

    // 전화 번호
    // @Column(length = 255)
    private String phoneNumber;

    // 국가 코드
    // @Column(length = 5)
    private String countryCode;
}

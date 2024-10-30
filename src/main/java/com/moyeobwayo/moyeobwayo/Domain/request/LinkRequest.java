package com.moyeobwayo.moyeobwayo.Domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkRequest {
    private int currentUserID;      // 기존 유저 ID
    // private String partyID;            // 현재 파티 ID
    private Long kakaoUserId;       // 카카오 유저 ID (고유 UUID)

    // private String code;            // 인가 코드 (기존 카카오 생성 시에만 필요)
}
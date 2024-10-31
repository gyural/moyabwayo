package com.moyeobwayo.moyeobwayo.Domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartyUserResponseDTO {
    private Long userId;
    private String userName;
    private String profileImage;  // null 또는 카카오 유저의 프로필 이미지
}

package com.moyeobwayo.moyeobwayo.Domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class TimeSlot {
    private Long userId;            // 사용자 ID 필드 추가
    private String userName;         // 사용자 이름 필드 추가
    LocalDateTime start;
    LocalDateTime end;
}
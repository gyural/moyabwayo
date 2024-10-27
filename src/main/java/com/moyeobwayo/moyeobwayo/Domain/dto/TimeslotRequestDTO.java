package com.moyeobwayo.moyeobwayo.Domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeslotRequestDTO {

    private Date selectedStartTime;
    private Date selectedEndTime;
    private int userId;
    private int dateId;
    private String binaryString;  // 1과 0으로 이루어진 문자열을 저장할 필드
}

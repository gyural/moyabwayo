package com.moyeobwayo.moyeobwayo.Domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TimeslotResponseDTO {

    private int slotId;
    private Date selectedStartTime;
    private Date selectedEndTime;
    private Long userId;
    private String partyId;
    private int dateId;
    private String byteString;


}
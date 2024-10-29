package com.moyeobwayo.moyeobwayo.Domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TimeslotResponseDTO {

    private Long slotId;
    private Long userId;
    private String partyId;
    private Long dateId;
    private String byteString;


}
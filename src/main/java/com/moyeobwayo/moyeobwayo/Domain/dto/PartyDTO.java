package com.moyeobwayo.moyeobwayo.Domain.dto;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartyDTO {
    private String partyId;
    private String partyName;
    private String userId;

    // Party 엔티티를 DTO로 변환하는 생성자
    public PartyDTO(Party party) {
        this.partyId = party.getPartyId();
        this.partyName = party.getPartyName();
        this.userId = party.getUserId();
    }
}

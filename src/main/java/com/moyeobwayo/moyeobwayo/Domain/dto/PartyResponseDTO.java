package com.moyeobwayo.moyeobwayo.Domain.dto;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class PartyResponseDTO {
    private Party party;
    private List<AvailableTime> availableTime;

    public void setParty(Party party) {
        this.party = party;

        // party의 dates와 timeslots에서 UserEntityDTO 생성
        this.party.getDates().forEach(date -> {
            date.getTimeslots().forEach(timeslot -> {
                if(timeslot.getUserEntity() != null){
                        timeslot.getUserEntity().setPassword(null);
                }
            });
        });
    }
}

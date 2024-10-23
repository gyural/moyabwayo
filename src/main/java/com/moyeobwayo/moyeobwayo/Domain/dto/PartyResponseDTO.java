package com.moyeobwayo.moyeobwayo.Domain.dto;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class PartyResponseDTO {
    private Party party;
    private List<AvailableTime> availableTime;

    public void setParty(Party party) {
        this.party = party;

        this.party.getDates().sort(Comparator.comparing(date -> date.getSelected_date()));
        // party의 dates와 timeslots에서 UserEntityDTO 생성
        this.party.getDates().forEach(date -> {
            if (date.getTimeslots() != null) {
                date.getTimeslots().forEach(timeslot -> {
                    if (timeslot.getUserEntity() != null) {
                        // 비밀번호 정보 제거
                        timeslot.getUserEntity().setPassword(null);
                    }
                });
            }
        });
        //또 date의 selectedDate가 오름차순 정령되게 하고 싶어
    }
}

package com.moyeobwayo.moyeobwayo.Domain.dto;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PartyResponseDTO {
    private Party party;
    private List<AvailableTime> availableTime;

    public void setParty(Party party) {
        this.party = party;

        // Set을 List로 변환 및 정렬
        // 기존 코드: this.party.getDates().sort(Comparator.comparing(DateEntity::getSelected_date));
        List<DateEntity> sortedDates = new ArrayList<>(this.party.getDates()); // Set -> List 변환
        sortedDates.sort(Comparator.comparing(DateEntity::getSelected_date)); // 정렬 수행
        this.party.setDates(new HashSet<>(sortedDates)); // 정렬된 데이터를 다시 Set으로 저장 (필요한 경우)

        // Timeslot 정보를 원하는 형식으로 변환하여 날짜별로 저장
        sortedDates.forEach(date -> { // 기존 코드: this.party.getDates().forEach(date -> {
            if (date.getTimeslots() != null) {
                List<TimeslotUserDTO> timeslotUserDTOs = new ArrayList<>(date.getTimeslots()) // Set -> List 변환
                        .stream()
                        .map(timeslot -> new TimeslotUserDTO(
                                timeslot.getUserEntity().getUserId(),
                                timeslot.getUserEntity().getUserName(),
                                timeslot.getByteString()
                        ))
                        .collect(Collectors.toList());

                // 기존 Timeslot 목록을 제거하고 변환된 DTO 리스트로 설정
                date.setTimeslots(null); // 기존 Timeslot 목록을 제거 (필요한 경우에만)
                date.setConvertedTimeslots(timeslotUserDTOs); // 변환된 DTO 추가
            }
        });
    }
}

package com.moyeobwayo.moyeobwayo.Domain.request.party;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * PartyCreateRequest
 * 전달받은 json을 해당 객체로 변환
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PartyCreateRequest {
    private int participants;           // 참여 인원 (party의 target_num)
    private String partyTitle;          // 파티 제목 (party의 party_name)
    private String partyDescription;    // 파티 설명 (party의 party_description)
    private Date startTime;             // 시작 시간 (party의 start_date)
    private Date endTime;               // 종료 시간 (party의 end_date)
    private List<Date> dates;           // 날짜 목록 (party의 party_id(pk)를 가져온 후 리스트 개수만큼 생성)
    private boolean decisionDate;       // **수정됨** 확정 여부를 나타내는 boolean 필드
    @JsonProperty("user_id") // JSON의 user_id를 Java의 userId로 매핑
    private String userId;
}

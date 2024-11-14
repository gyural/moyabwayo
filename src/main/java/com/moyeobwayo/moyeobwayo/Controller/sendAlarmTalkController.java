package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Service.kakaotalkalarmService;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alimtalk")
public class sendAlarmTalkController {

    private final kakaotalkalarmService kakaotalkalarmService;

    // Constructor-based injection ensures the service is correctly initialized
    @Autowired
    public sendAlarmTalkController(kakaotalkalarmService kakaotalkalarmService) {
        this.kakaotalkalarmService = kakaotalkalarmService;
    }

    /**
     * 투표 완료 알림을 보내는 API
     * @param partyId 파티 ID
     * @param to 수신자 전화번호
     * @return 성공 여부
     */
    @PostMapping("/sendVotingCompletion")
    public String sendVotingCompletionAlimTalk(
            @RequestParam String partyId,
            @RequestParam String to) {

        try {
            // 데이터베이스를 사용하지 않고 하드코딩된 파티 정보 생성
            Party party = getDummyParty(partyId);
            if (party == null) {
                return "파티 정보를 찾을 수 없습니다.";
            }

            // 예시로 상위 3개의 시간대 데이터를 제공 (예: "시간대 1", "시간대 2", "시간대 3")
            List<String> topTimeSlots = List.of("시간대 1", "시간대 2", "시간대 3");

            // 알림 전송
            kakaotalkalarmService.sendVotingCompletionAlimTalk(party, topTimeSlots, to);

            return "투표 완료 알림이 성공적으로 전송되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "알림 전송 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 더미 파티 정보를 반환하는 메서드
     * @param partyId 파티 ID
     * @return Party 객체
     */
    private Party getDummyParty(String partyId) {
        // 파티 ID에 따라 다른 더미 파티 정보를 생성
        if ("1".equals(partyId)) {
            Party party = new Party();
            party.setPartyName("모임 A");
            party.setUserId("파티장 홍길동");
            return party;
        }
        return null;  // 파티가 없으면 null 반환
    }
}

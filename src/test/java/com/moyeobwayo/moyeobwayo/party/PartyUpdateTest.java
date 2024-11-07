//package com.moyeobwayo.moyeobwayo.party;
//
//import com.moyeobwayo.moyeobwayo.Domain.Party;
//import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCreateRequest;
//import com.moyeobwayo.moyeobwayo.Repository.PartyStringIdRepository;
//import com.moyeobwayo.moyeobwayo.Service.PartyService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//public class PartyUpdateTest {
//
//    @Autowired
//    private PartyService partyService;
//
//    @Autowired
//    private PartyStringIdRepository partyStringIdRepository;
//
//    @Test
//    @Transactional
//    public void 파티수정test() {
//        // 기존 파티 생성
//        Party party = new Party();
//        party.setPartyName("Original Party Name");
//        party.setPartyDescription("Original Description");
//        party.setTargetNum(5);
//        party.setCurrentNum(0);
//        party.setStartDate(new Date());
//        party.setEndDate(new Date(System.currentTimeMillis() + 86400000)); // 1일 후
//        party.setUserId("1234");
//
//        // 파티 저장
//        Party savedParty = partyStringIdRepository.save(party);
//
//        // 파티 수정 요청
//        PartyCreateRequest updateRequest = new PartyCreateRequest(
//                10, // 참가 인원 변경
//                "Updated Party Name", // 파티 이름 변경
//                "Updated Description", // 파티 설명 변경
//                new Date(), // 시작 날짜
//                new Date(System.currentTimeMillis() + 172800000), // 종료 날짜 (2일 후)
//                List.of(new Date()), // 날짜 리스트
//                new Date(System.currentTimeMillis() + 86400000), // 확정 날짜 (1일 후)
//                "5678" // 변경된 user_id
//        );
//
//        // 파티 업데이트 수행
//        partyService.updateParty(savedParty.getPartyId(), updateRequest);
//
//        // 수정된 파티 조회
//        Party updatedParty = partyStringIdRepository.findById(savedParty.getPartyId()).orElseThrow();
//
//        // 업데이트 결과 검증
//        assertThat(updatedParty.getPartyName()).isEqualTo("Updated Party Name");
//        assertThat(updatedParty.getPartyDescription()).isEqualTo("Updated Description");
//        assertThat(updatedParty.getTargetNum()).isEqualTo(10);
//        assertThat(updatedParty.getCurrentNum()).isEqualTo(0); // 기존 값 유지
//        assertThat(updatedParty.getUserId()).isEqualTo(5678); // user_id 변경 확인
//        assertThat(updatedParty.getStartDate()).isNotNull();
//        assertThat(updatedParty.getEndDate()).isAfter(updatedParty.getStartDate()); // 종료 날짜가 시작 날짜보다 이후인지 확인
//    }
//}

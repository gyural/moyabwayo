package com.moyeobwayo.moyeobwayo.party;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Repository.PartyStringIdRepository;  // 변경된 Repository 사용
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PartyCreateTest {

    @Autowired
    private PartyStringIdRepository partyStringIdRepository;  // 변경된 Repository 사용

    @Test
    @Transactional
    public void 파티생성test() {
        // 파티 세팅
        Party party = new Party();
        party.setPartyName("Spring Boot Party");
        party.setPartyDescription("This is a test party for Spring Boot.");
        party.setTargetNum(10);
        party.setCurrentNum(0); // 현재 인원은 0으로 초기화
        party.setStartDate(new Date());
        party.setEndDate(new Date(System.currentTimeMillis() + 86400000)); // 1일 후
        party.setUserId("1234"); // 새로 추가된 user_id 필드 설정

        // 파티 저장
        Party savedParty = partyStringIdRepository.save(party);

        // 결과 검증
        assertThat(savedParty).isNotNull();
        assertThat(savedParty.getPartyId()).isEqualTo("Spring Boot Party");
        assertThat(savedParty.getPartyDescription()).isEqualTo("This is a test party for Spring Boot.");
        assertThat(savedParty.getTargetNum()).isEqualTo(10);
        assertThat(savedParty.getCurrentNum()).isEqualTo(0);
        assertThat(savedParty.getUserId()).isEqualTo(1234); // user_id 검증
        assertThat(savedParty.getStartDate()).isNotNull();
        assertThat(savedParty.getEndDate()).isNotNull();
        // 날짜 검증: 현재 시간보다 미래의 날짜인지
        assertThat(savedParty.getEndDate()).isAfter(savedParty.getStartDate());
    }
}

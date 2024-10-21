package com.moyeobwayo.moyeobwayo.Timeslot;

import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Repository.DateEntityRepsitory;
import com.moyeobwayo.moyeobwayo.Repository.PartyRepository;
import com.moyeobwayo.moyeobwayo.Repository.TimeslotRepository;
import com.moyeobwayo.moyeobwayo.Repository.UserEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class TimeslotRepositoryTest {

    @Autowired
    private TimeslotRepository timeslotRepository;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private DateEntityRepsitory dateEntityRepsitory;

    private UserEntity user;
    private Party party;
    private DateEntity dateEntity;

    @BeforeEach
    public void setup() {
        // Create and save a Party
        party = new Party();
        party.setPartyId("testPartyId");
        party.setPartyName("Test Party");
        party.setCurrentNum(1);
        party.setTargetNum(5);
        party.setStartDate(new Date());
        partyRepository.save(party);  // Party를 저장

        // Create and save a UserEntity
        user = new UserEntity();
        user.setUserName("testUserName");
        user.setParty(party);  // UserEntity와 Party 관계 설정
        userEntityRepository.save(user);  // 반드시 먼저 UserEntity를 저장

        // Create and save a DateEntity
        dateEntity = new DateEntity();
        dateEntity.setSelected_date(new Date());  // 설정 날짜
        dateEntity.setParty(party);  // Party 설정
        dateEntityRepsitory.save(dateEntity);  // DateEntity 저장

        // Create and save a Timeslot
        Timeslot timeslot = new Timeslot();
        timeslot.setUserEntity(user);  // UserEntity 설정
        timeslot.setDate(dateEntity);  // DateEntity 설정
        timeslot.setSelectedStartTime(new Date());
        timeslot.setSelectedEndTime(new Date());
        timeslotRepository.save(timeslot);  // Timeslot 저장
    }

    @Test
    public void testExistsUserInPartyTimeslot() {
        // 저장된 user의 ID를 가져옴
        Long savedUserId = user.getUserId();  // 자동 생성된 user ID 사용

        // 실제로 데이터베이스에 저장된 값으로 메서드를 테스트
        boolean exists = timeslotRepository.existsUserInPartyTimeslot(savedUserId, "testPartyId");
        assertThat(exists).isTrue();  // 사용자가 파티 타임슬롯에 존재하는지 확인

        // 존재하지 않는 userId나 partyId로 테스트
        boolean userDoesNotExist = timeslotRepository.existsUserInPartyTimeslot(333L, "testPartyId");
        assertThat(userDoesNotExist).isFalse();  // 존재하지 않는 사용자 확인

        boolean partyDoesNotExist = timeslotRepository.existsUserInPartyTimeslot(savedUserId, "invalidPartyId");
        assertThat(partyDoesNotExist).isFalse();  // 존재하지 않는 파티 확인
    }
}
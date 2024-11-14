package com.moyeobwayo.moyeobwayo.Alarm;

import com.moyeobwayo.moyeobwayo.Domain.Alarm;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Repository.AlarmRepository;
import com.moyeobwayo.moyeobwayo.Repository.PartyRepository;
import com.moyeobwayo.moyeobwayo.Service.AlarmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AlarmUpdateTest {

    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    AlarmService alarmService;

    @Autowired
    PartyRepository partyRepository;
    @Test
    public void updateAlarmTest() {
        // 1. 테스트용 알람 생성
        Party party = new Party();
        party.setPartyId("testtestID");
        party = partyRepository.save(party);

        UserEntity user = new UserEntity();
        user.setParty(party);

        Alarm alarm = new Alarm();
        alarm.setAlarm_on(true);
        alarm.setUserEntity(user);
        alarm.setParty((party));
        // 여기에 UserEntity와 Party 객체도 필요하다면 추가해야 합니다.
        alarmRepository.save(alarm); // 데이터베이스에 저장

        // 2. 저장된 알람의 ID로 알람 업데이트
        Long alarmId = alarm.getAlarmId();
        Alarm updatedAlarm = alarmService.updateAlarm(alarmId, false); // alarm_on 값을 false로 변경

        // 3. 업데이트된 알람의 값을 검증
        assertNotNull(updatedAlarm); // 업데이트된 알람이 존재해야 한다.
        assertEquals(false, updatedAlarm.isAlarm_on()); // alarm_on 값이 false로 변경되었는지 확인
    }
}
package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.Alarm;
import com.moyeobwayo.moyeobwayo.Repository.AlarmRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmRepository alarmRepository;
    public Alarm getAlarm(String kakaoId, String partyId) {
        Optional<Alarm> alarm = alarmRepository.findAlarmByPartyIdAndKakaoUserId(partyId, kakaoId);
        if (alarm.isPresent()) {
            return alarm.get();
        } else {
            throw new EntityNotFoundException("Alarm not found");
        }
    }
}

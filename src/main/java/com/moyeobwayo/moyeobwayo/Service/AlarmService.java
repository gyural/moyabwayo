package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.Alarm;
import com.moyeobwayo.moyeobwayo.Repository.AlarmRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

    // Service
    public Alarm updateAlarm(Long id, boolean alarmOn) {
        Alarm alarm = alarmRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        alarm.setAlarm_on(alarmOn);
        return alarmRepository.save(alarm);
    }

    // Exception Handling
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alarm not found.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

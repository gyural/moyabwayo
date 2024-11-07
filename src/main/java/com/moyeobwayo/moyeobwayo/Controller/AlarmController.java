package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Domain.Alarm;
import com.moyeobwayo.moyeobwayo.Domain.request.alarm.AlarmUpdateRequest;
import com.moyeobwayo.moyeobwayo.Service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/alarm")
public class AlarmController {
    private final AlarmService alarmService;

    @GetMapping("/")
    public ResponseEntity<?> getAlarm(
            @RequestParam("kakaoId") String kakaoId, // 쿼리 파라미터로 kakaoId 받음
            @RequestParam("partyId") String partyId  // 쿼리 파라미터로 partyId 받음
            //@PageableDefault Pageable pageable       // 페이징 처리
    ) {
        try {
            // alarmService에서 쿼리 파라미터 사용
            Alarm alarms = alarmService.getAlarm(kakaoId, partyId);
            return ResponseEntity.ok(alarms); // 성공 시 알람 리스트 반환
        } catch (Exception e) {
            // 예외 처리 (적절한 예외 처리를 여기에 추가)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAlarm(@PathVariable("id") Long alarmId,
                                         @RequestBody AlarmUpdateRequest request) {
        if (!alarmId.equals(request.getId())) {
            throw new IllegalArgumentException("Path variable ID and request body ID must match.");
        }
        Alarm updatedAlarm = alarmService.updateAlarm(alarmId, request.isAlarmOn());
        return ResponseEntity.ok(updatedAlarm);
    }
}

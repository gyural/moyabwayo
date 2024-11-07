package com.moyeobwayo.moyeobwayo.Domain.request.alarm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AlarmUpdateRequest {
    private Long id;
    private boolean alarmOn;
}
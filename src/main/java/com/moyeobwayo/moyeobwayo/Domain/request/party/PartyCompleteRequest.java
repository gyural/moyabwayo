/**
 * 목적: controller가 json으로 받은 body를 하나의 객체로 묶어 처리하기 위해 다음과 같은 클래스 생성
 */
package com.moyeobwayo.moyeobwayo.Domain.request.party;

import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Getter
@Setter
@Service
@AllArgsConstructor
@NoArgsConstructor
public class PartyCompleteRequest {
    private Long userId;
    private Date completeTime;
    private Date endTime;
    private String locationName;
    private long dateId;
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}

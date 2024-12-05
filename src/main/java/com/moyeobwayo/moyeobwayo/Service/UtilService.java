package com.moyeobwayo.moyeobwayo.Service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class UtilService {
    // 전화번호 형식 변환(가공)
    // db 에 저장된 형태 : '10-1234-5678' -> 변환 : '01012345678'
    public String formatPhoneNumber(String countryCode, String phoneNumber) {
        if (countryCode == null || phoneNumber == null) {
            throw new IllegalArgumentException("전화번호 또는 국가 코드가 null입니다.");
        }
        // 하이픈 제거 및 숫자만 추출
        String formattedNumber = phoneNumber.replaceAll("[^0-9]", "");

        // 전화번호의 첫 자리가 0이 아니면 0 추가
        if (!formattedNumber.startsWith("0")) {
            formattedNumber = "0" + formattedNumber;
        }

        return formattedNumber; // 형식 예) 01012345678
    }

    public String subtractMinutesFromCompleteTime(Date completeTimeInDate, int minutesToSubtract) {
        // 1. Date 객체를 LocalDateTime으로 변환 (KST 기준)
        LocalDateTime kstTime = completeTimeInDate.toInstant()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();

        // 2. 지정된 분(minutesToSubtract)만큼 시간에서 빼기
        LocalDateTime adjustedTime = kstTime.minusMinutes(minutesToSubtract);

        // 3. 결과를 지정된 포맷으로 문자열 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return adjustedTime.format(formatter);
    }

    public boolean isTimeEarlierThanNow(Date reservationTimeInKst, int minutesToSubtract) {
        // 1. Date 객체를 LocalDateTime으로 변환 (KST 기준)
        LocalDateTime kstTime = reservationTimeInKst.toInstant()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();

        // 2. 지정된 분(minutesToSubtract)만큼 시간에서 빼기
        LocalDateTime adjustedTime = kstTime.minusMinutes(minutesToSubtract);

        // 3. 현재 시간 구하기 (KST 기준)
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 4. 조정된 시간이 현재 시간보다 이른지 판별
        return adjustedTime.isBefore(now);
    }
}

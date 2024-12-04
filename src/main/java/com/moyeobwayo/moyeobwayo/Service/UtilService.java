package com.moyeobwayo.moyeobwayo.Service;

import org.springframework.stereotype.Service;

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
}

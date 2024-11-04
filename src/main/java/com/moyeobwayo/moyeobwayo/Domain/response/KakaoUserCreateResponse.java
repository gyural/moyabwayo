package com.moyeobwayo.moyeobwayo.Domain.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KakaoUserCreateResponse {
    private String token;
    private boolean talkCalendarOn;


}
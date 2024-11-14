package com.moyeobwayo.moyeobwayo.Domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class GetMeetListByKakaoIdRequest {
    private Long kakaoUserId;
    private int page;  // 요청 페이지 번호
    private int size;  // 한 페이지당 데이터 개수
}

package com.moyeobwayo.moyeobwayo.party;

import com.moyeobwayo.moyeobwayo.Domain.Alarm;
import com.moyeobwayo.moyeobwayo.Domain.KakaoProfile;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCompleteRequest;
import com.moyeobwayo.moyeobwayo.Repository.KakaoProfileRepository;
import com.moyeobwayo.moyeobwayo.Repository.PartyRepository;
import com.moyeobwayo.moyeobwayo.Repository.UserEntityRepository;
import com.moyeobwayo.moyeobwayo.Service.KakaoUserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class PartyTest {

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 파티 확정 관련 테스트를 위한 하위 클래스
    @Nested
    public class PartyCompleteTest {

        private PartyCompleteRequest validRequest;

        @BeforeEach
        public void setup() {
            // 유효한 요청 데이터 기본 설정
            validRequest = new PartyCompleteRequest();
            validRequest.setUserId(1L);
            validRequest.setCompleteTime(new Date());
        }

        @Test
        public void 필수값_검증() throws Exception {
            // 필수값 누락된 요청 생성
            PartyCompleteRequest request = new PartyCompleteRequest();
            request.setUserId(null);  // userId 누락
            request.setCompleteTime(null);  // completeTime 누락

            // 요청 실행 및 검증
            mockMvc.perform(post("/api/v1/party/complete/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))  // 요청 직렬화
                    .andExpect(status().isBadRequest())  // 400 응답을 기대
                    .andExpect(jsonPath("$.message").value("Error: User ID is required."));  // JSON 응답 검증
        }

        @Test
        public void 파티존재_검증() throws Exception {
            // 파티가 존재하지 않는 경우 설정

            mockMvc.perform(post("/api/v1/party/complete/999")  // 존재하지 않는 ID
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())  // 400 응답 기대
                    .andExpect(jsonPath("$.message").value("Error: Party not found"));
        }

        @Test
        @Transactional
        public void testCompleteParty_DecisionDateSaved() throws Exception {
            // 파티와 유저 설정
            Party party = new Party();
            party.setPartyName("testParty"); // 파티 이름 설정
            party = partyRepository.save(party); // 실제 DB에 저장

            UserEntity user = new UserEntity();
            user.setUserId(1L);  // 유저 ID 설정
            userEntityRepository.save(user); // 유저도 DB에 저장

            // 요청 데이터 설정
            PartyCompleteRequest request = new PartyCompleteRequest();
            Date completeDate = new Date();  // 완료 날짜 설정
            request.setCompleteTime(completeDate);
            request.setUserId(user.getUserId());  // 유저 ID 설정

            // 요청 실행
            mockMvc.perform(post("/api/v1/party/complete/" + party.getPartyId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());  // 성공적인 응답 기대

            // 파티가 DB에 저장되었는지 확인
            Party updatedParty = partyRepository.findById(party.getPartyId()).orElse(null);
            assertNotNull(updatedParty); // 업데이트된 파티가 null이 아니어야 함
            assertEquals(completeDate, updatedParty.getDecisionDate()); // 완료 날짜 검증
        }

        @Test
        @Transactional
        public void 알람설정검증을_통한_확정메시지_보내기(){
            Party party = new Party();
            party.setPartyName("testParty");

            // Alarm 객체를 각각 Mocking
            Alarm mockAlarm1 = mock(Alarm.class);
            Alarm mockAlarm2 = mock(Alarm.class);

            KakaoProfile kakaoProfile1 = mock(KakaoProfile.class);
            KakaoProfile kakaoProfile2 = mock(KakaoProfile.class);

            UserEntity user1 = mock(UserEntity.class);
            UserEntity user2 = mock(UserEntity.class);

            // user1: 알람 켜져 있음
            when(mockAlarm1.isAlarm_on()).thenReturn(true);
            when(user1.getAlarm()).thenReturn(mockAlarm1);
            when(user1.getKakaoProfile()).thenReturn(kakaoProfile1);
            when(kakaoProfile1.isAlarm_off()).thenReturn(false);

            // user2: 알람 꺼져 있음
            when(mockAlarm2.isAlarm_on()).thenReturn(false);
            when(user2.getAlarm()).thenReturn(mockAlarm2);
            when(user2.getKakaoProfile()).thenReturn(kakaoProfile2);
            when(kakaoProfile2.isAlarm_off()).thenReturn(true);


            List<UserEntity> users = Arrays.asList(user1, user2);

            // 필요한 레포지토리들을 Mocking
            KakaoProfileRepository mockKakaoProfileRepository = mock(KakaoProfileRepository.class);
            UserEntityRepository mockUserEntityRepository = mock(UserEntityRepository.class);

            // Mock 레포지토리를 주입한 KakaoUserService 스파이 생성
            KakaoUserService mockPartyService = spy(new KakaoUserService(mockKakaoProfileRepository, mockUserEntityRepository));
            doNothing().when(mockPartyService).sendCompleteMessage(any(KakaoProfile.class), any(Party.class), any(Date.class));

            // 호출할 날짜 설정
            Date completeDate = new Date();

            // 실제 메서드 호출
            mockPartyService.sendKakaoCompletMesage(users, party, completeDate);

            // 검증: 알람이 켜져 있는 유저에게만 메시지 전송
            verify(mockPartyService, times(1)).sendCompleteMessage(user1.getKakaoProfile(), party, completeDate);
            verify(mockPartyService, never()).sendCompleteMessage(user2.getKakaoProfile(), party, completeDate);
        }
    }
}
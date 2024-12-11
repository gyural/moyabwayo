package com.moyeobwayo.moyeobwayo.auth;


import com.moyeobwayo.moyeobwayo.Domain.KakaoProfile;
import com.moyeobwayo.moyeobwayo.Domain.request.auth.TokenValidateRequest;
import com.moyeobwayo.moyeobwayo.Repository.KakaoProfileRepository;
import com.moyeobwayo.moyeobwayo.Service.AuthService;
import com.moyeobwayo.moyeobwayo.Service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.matchers.JUnitMatchers.containsString;

@Transactional
@SpringBootTest
public class TokenValidateServiceTest {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private KakaoProfileRepository kakaoProfileRepository;
    @Autowired
    private AuthService authService;

    private KakaoProfile validKakaoProfile;
    private String validToken;
    private String invalidToken;
    private String validTokenButNotExistUser;

    @BeforeEach
    void setUp() {
        // 데이터베이스에 유효한 KakaoProfile 저장
        validKakaoProfile = new KakaoProfile();
        validKakaoProfile.setKakaoUserId(777777L); // 유저 ID
        validKakaoProfile.setNickname("validNickName");
        validKakaoProfile.setProfile_image("validProfileImage");
        kakaoProfileRepository.save(validKakaoProfile);

        // 토큰 생성
        validToken = jwtService.generateToken(
                validKakaoProfile.getKakaoUserId(),
                validKakaoProfile.getNickname(),
                validKakaoProfile.getProfile_image()
        );

        invalidToken = validToken + "invalid";

        validTokenButNotExistUser = jwtService.generateToken(
                444444L, // 존재하지 않는 유저 ID
                "invalidNickName",
                "invalidProfileImage"
        );
    }

    @Test
    @DisplayName("isValidateRequest 메서드: 유효하지 않은 요청값 (토큰 없음)")
    void testIsValidateRequestInvalid() {
        TokenValidateRequest request = new TokenValidateRequest(null);
        boolean result = authService.isValidateRequest(request);
        assertThat("토큰이 null일 경우 isValidateRequest는 false를 반환해야 합니다.", result, is(false));
    }

    @Test
    @DisplayName("isValidateRequest 메서드: 유효한 요청값 (토큰 존재)")
    void testIsValidateRequestValid() {
        TokenValidateRequest request = new TokenValidateRequest(validToken);
        boolean result = authService.isValidateRequest(request);
        assertThat("토큰이 존재할 경우 isValidateRequest는 true를 반환해야 합니다.", result, is(true));
    }

    @Test
    @DisplayName("isValidateToken 메서드: 올바른 토큰 (유효한 사용자)")
    void testIsValidateTokenValidUser() {
        boolean result = authService.isValidateToken(validToken);
        assertThat("유효한 사용자의 토큰은 isValidateToken이 true를 반환해야 합니다.", result, is(true));
    }

    @Test
    @DisplayName("isValidateToken 메서드: 올바른 토큰 (존재하지 않는 사용자)")
    void testIsValidateTokenNonExistentUser() {
        boolean result = authService.isValidateToken(validTokenButNotExistUser);
        assertThat("존재하지 않는 사용자의 토큰은 isValidateToken이 false를 반환해야 합니다.", result, is(false));
    }

    @Test
    @DisplayName("isValidateToken: 유효하지 않은 토큰")
    void testIsValidateTokenInvalidToken() {
        // RuntimeException이 발생해야 하므로 해당 예외를 테스트
        Exception exception = assertThrows(
                RuntimeException.class,  // RuntimeException이 발생할 것으로 예상
                () -> authService.isValidateToken(invalidToken)
        );

        // 발생한 예외가 RuntimeException인지를 체크
        assertThat("유효하지 않은 토큰이 입력되었을 때 RuntimeException이 발생해야 합니다.",
                exception, instanceOf(RuntimeException.class));

        // 원인 예외가 JwtException인지 확인
        assertThat("RuntimeException의 원인 예외는 JwtException이어야 합니다.",
                exception.getCause(), instanceOf(io.jsonwebtoken.JwtException.class));
    }
}

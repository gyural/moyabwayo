package com.moyeobwayo.moyeobwayo.auth;

import com.moyeobwayo.moyeobwayo.Controller.AuthController;
import com.moyeobwayo.moyeobwayo.Domain.request.auth.TokenValidateRequest;
import com.moyeobwayo.moyeobwayo.Domain.response.auth.TokenValidateResponse;
import com.moyeobwayo.moyeobwayo.Service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TokenValidateContollerTest {

    @Autowired
    private AuthController authController;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("Invalid 요청값: 유효하지 않은 요청값 처리 테스트")
    void invalidRequestValueTest() {
        // Given 빈 요청값이 주어졌을 때
        TokenValidateRequest invalidRequest = new TokenValidateRequest(); // 빈 요청값

        // Mock: isValidateRequest가 false 반환하게 설정
        when(authService.isValidateRequest(invalidRequest)).thenReturn(false);

        // When response가 basRequest가 반환되어야함
        ResponseEntity<?> response = authController.validateToken(invalidRequest);

        // Then
        assertEquals(400, response.getStatusCodeValue()); // Bad Request
        assertEquals("Requset Value invalid", response.getBody());
        verify(authService, times(1)).isValidateRequest(invalidRequest);
        verify(authService, never()).isValidateToken(anyString()); // 토큰 검증 메서드는 호출되지 않아야 함
    }

    @Test
    @DisplayName("Valid 요청값: 정상 서비스 함수 호출 테스트")
    void validRequestValueTest() {
        // Given
        TokenValidateRequest validRequest = new TokenValidateRequest();
        validRequest.setToken("valid_token");

        // Mock: isValidateRequest와 isValidateToken 설정
        when(authService.isValidateRequest(validRequest)).thenReturn(true);
        when(authService.isValidateToken("valid_token")).thenReturn(true);

        // When
        ResponseEntity<?> response = authController.validateToken(validRequest);

        // Then
        assertEquals(200, response.getStatusCodeValue()); // OK
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof TokenValidateResponse);
        assertTrue(((TokenValidateResponse) response.getBody()).isValidate());

        verify(authService, times(1)).isValidateRequest(validRequest);
        verify(authService, times(1)).isValidateToken("valid_token");
    }
}

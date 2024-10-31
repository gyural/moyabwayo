package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Domain.KakaoProfile;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.response.UserResponse;
import com.moyeobwayo.moyeobwayo.Service.JwtService;
import com.moyeobwayo.moyeobwayo.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moyeobwayo.moyeobwayo.Domain.request.user.UserLoginRequest;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/user")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    // 생성자를 통한 서비스 주입
    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // 요약: 로그인할 때, 만약 카카오 유저라면 정보를 jwt 토큰에 담아 전달
    // 유저 로그인 과정에서 kakao_user_id null 이 아닌 경우에 그 값을 이용해 kakao_profile에 접근
    // kakao_profile에서 (kakao_user_id, nickname, profile_image)를 jwt 토큰에 전달
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request) {
        try {
            // 서비스 호출하여 사용자 인증
            Optional<UserResponse> user = userService.loginOrRegister(
                    request.getUserName(),
                    request.getPassword(),
                    request.getPartyId(),
                    request.isKakao()
            );

            if (user.isPresent()) {
                UserResponse userResponse = user.get();

                // 카카오 유저 여부 확인 및 토큰에 포함할 정보 가져오기
                KakaoProfile kakaoProfile = userResponse.getUser().getKakaoProfile();
                if (kakaoProfile != null) {
                    // JWT 생성 서비스 호출하여 토큰 생성
                    String jwtToken = jwtService.generateToken(
                            kakaoProfile.getKakaoUserId(),
                            kakaoProfile.getNickname(),
                            kakaoProfile.getProfile_image()
                    );

                    // UserResponse에 토큰 추가
                    userResponse.setToken(jwtToken);
                }

                return ResponseEntity.ok(userResponse);
            } else {
                // 로그인 실패 시, 오류 메시지 반환
                return ResponseEntity.status(401).body("Login failed: Duplicate username in the same party or invalid credentials");
            }
        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 500 오류 반환
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류: " + e.getMessage());
        }
    }
}

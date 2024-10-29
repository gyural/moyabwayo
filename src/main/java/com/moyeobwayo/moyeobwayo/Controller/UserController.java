package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.response.UserResponse;
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

    // 생성자를 통한 서비스 주입
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request) {
        // 서비스 호출하여 사용자 인증
        Optional<UserResponse> user = userService.loginOrRegister(request.getUserName(), request.getPassword(), request.getPartyId(), request.isKakao(), request.getKakaoUserId());

        if (user.isPresent()) {
            // 로그인 성공 시, 사용자 정보를 반환
            return ResponseEntity.ok(user.get());
        } else {
            // 로그인 실패 시, 오류 메시지 반환
            return ResponseEntity.status(401).body("Login failed: Duplicate username in the same party or invalid credentials");
        }
    }
}

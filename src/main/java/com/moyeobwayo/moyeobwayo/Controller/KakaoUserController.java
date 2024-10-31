package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Domain.KakaoProfile;
import com.moyeobwayo.moyeobwayo.Domain.request.KakaoUserCreateRequest;
import com.moyeobwayo.moyeobwayo.Domain.request.KakaoUserUpdateRequest;
import com.moyeobwayo.moyeobwayo.Domain.request.LinkRequest;
import com.moyeobwayo.moyeobwayo.Service.KakaoUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/kakaoUser")
public class KakaoUserController {
    private final KakaoUserService kakaoUserService;

    public KakaoUserController(KakaoUserService kakaoUserService) {
        this.kakaoUserService = kakaoUserService;
    }


    @PostMapping("/create")
    public ResponseEntity<?> createKakaoUser(@RequestBody KakaoUserCreateRequest request) {
        KakaoProfile profile = kakaoUserService.createUser(request.getCode());
        return ResponseEntity.ok(profile);
    }


    @PostMapping("/link")
    public ResponseEntity<?> linkKakaoUser(@RequestBody LinkRequest request) {
        try {
            // 전달받은 kakao_user_id를 사용하여 기존 유저와 카카오 유저 연결
            boolean isLinked = kakaoUserService.linkUserToKakaoWithKakaoId(
                    request.getCurrentUserID(),    // LinkRequest의 필드 사용
                    request.getKakaoUserId()
            );
            if (isLinked) {
                return ResponseEntity.ok("Kakao user successfully linked with the existing user.");
            } else {
                return ResponseEntity.badRequest().body("Failed to link Kakao user. Please check the provided IDs.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/refuse")
    public ResponseEntity<?> updateKakaoUserSettings(@RequestBody KakaoUserUpdateRequest request) {
        try {
            boolean isUpdated = kakaoUserService.updateKakaoUserSettings(
                    request.getKakao_user_id(),
                    request.isKakao_message_allow(),
                    request.isAlarm_off()
            );
            if (isUpdated) {
                return ResponseEntity.ok("Kakao user settings updated successfully.");
            } else {
                return ResponseEntity.badRequest().body("Failed to update Kakao user settings. Please check the provided ID.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}

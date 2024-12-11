package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.dto.KakaoUserInfoDTO;
import com.moyeobwayo.moyeobwayo.Domain.request.auth.TokenValidateRequest;
import com.moyeobwayo.moyeobwayo.Repository.KakaoProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final KakaoProfileRepository kakaoProfileRepository;

    public boolean isValidateToken(String targetToken){

        KakaoUserInfoDTO kakaoUserInfo = jwtService.decodeTokenToUserInfo(targetToken);
        Long targetKakaoUserId = kakaoUserInfo.getKakao_user_id();

        return kakaoProfileRepository.existsById(targetKakaoUserId);

    }

    public boolean isValidateRequest(TokenValidateRequest tokenValidateRequest){
        if (tokenValidateRequest.getToken() == null){
            return false;
        }
        return true;
    }
}

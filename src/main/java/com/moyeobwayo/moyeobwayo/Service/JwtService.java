package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.dto.KakaoUserInfoDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

@Service
public class JwtService {
    @Value("${SECRET_KEY}")
    private String secretKey;

    public String generateToken(Long kakaoUserId, String nickname, String profileImage) {
        System.out.println(profileImage);
        return Jwts.builder()
                .claim("kakao_user_id", kakaoUserId)
                .claim("nickname", nickname)
                .claim("profile_image", profileImage)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 만료 시간 설정
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public KakaoUserInfoDTO decodeTokenToUserInfo(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            Long kakaoUserId = claims.get("kakao_user_id", Long.class);
            String nickname = claims.get("nickname", String.class);
            String profileImage = claims.get("profile_image", String.class);

            return new KakaoUserInfoDTO(kakaoUserId, nickname, profileImage);

        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서 Claims를 추출
            Claims claims = e.getClaims();
            if (claims != null) {
                Long kakaoUserId = claims.get("kakao_user_id", Long.class);
                String nickname = claims.get("nickname", String.class);
                String profileImage = claims.get("profile_image", String.class);

                // 경고: 만료된 정보를 사용하는 경우 별도의 처리 필요
                return new KakaoUserInfoDTO(kakaoUserId, nickname, profileImage);
            } else {
                // 만료된 Claims도 없는 경우
                throw new RuntimeException("만료된 토큰이며 Claims를 읽을 수 없습니다.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException("토큰 디코딩 실패", e);
        }
    }
}


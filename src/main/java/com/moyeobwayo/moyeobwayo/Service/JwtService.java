package com.moyeobwayo.moyeobwayo.Service;

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
}


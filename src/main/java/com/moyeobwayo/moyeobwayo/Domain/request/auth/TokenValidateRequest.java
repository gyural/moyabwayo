package com.moyeobwayo.moyeobwayo.Domain.request.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class TokenValidateRequest {
    private String token;
}

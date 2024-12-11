package com.moyeobwayo.moyeobwayo.Domain.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TokenValidateResponse {
    private boolean isValidate;
}

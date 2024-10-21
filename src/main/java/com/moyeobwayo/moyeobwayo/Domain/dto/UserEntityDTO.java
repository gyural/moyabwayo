package com.moyeobwayo.moyeobwayo.Domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class UserEntityDTO {
    private Long userId;
    private String username;
}

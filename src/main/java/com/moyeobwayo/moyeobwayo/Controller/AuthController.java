package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Domain.request.auth.TokenValidateRequest;
import com.moyeobwayo.moyeobwayo.Domain.response.auth.TokenValidateResponse;
import com.moyeobwayo.moyeobwayo.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/token/validate")
    public ResponseEntity<?> validateToken(@RequestBody TokenValidateRequest tokenValidateRequest) {

        if (authService.isValidateRequest(tokenValidateRequest) == false){
            return ResponseEntity.badRequest().body("Requset Value invalid");
        }

        try{
            String targetToken = tokenValidateRequest.getToken();
            boolean isValidate =  authService.isValidateToken(targetToken);
            return ResponseEntity.ok(new TokenValidateResponse(isValidate));
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

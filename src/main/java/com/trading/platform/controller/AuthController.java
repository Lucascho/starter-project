package com.trading.platform.controller;

import com.trading.platform.dto.ErrorResponse;
import com.trading.platform.dto.LoginRequest;
import com.trading.platform.dto.LoginResponse;
import com.trading.platform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of("UNAUTHORIZED", "帳號或密碼錯誤"));
        }
        return ResponseEntity.ok(new LoginResponse(token));
    }
}

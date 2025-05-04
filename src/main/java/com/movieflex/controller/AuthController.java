package com.movieflex.controller;

import com.movieflex.auth.entities.RefreshToken;
import com.movieflex.auth.entities.User;
import com.movieflex.auth.service.AuthService;
import com.movieflex.auth.service.JwtService;
import com.movieflex.auth.service.RefreshTokenService;
import com.movieflex.auth.utils.AuthResponse;
import com.movieflex.auth.utils.LoginRequest;
import com.movieflex.auth.utils.RefreshTokenRequest;
import com.movieflex.auth.utils.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest){

        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest){

        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());
        User user=refreshToken.getUser();

        String accesToken=jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accesToken)
                .refreshToken(refreshToken.getRefreshToken()).build());
    }
}

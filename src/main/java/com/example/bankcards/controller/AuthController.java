package com.example.bankcards.controller;

import com.example.bankcards.config.auth.JwtAuthResponse;
import com.example.bankcards.dto.UserSignInDto;
import com.example.bankcards.dto.UserSignUpDto;
import com.example.bankcards.service.impl.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register new user")
    @PostMapping("/sign-up")
    public ResponseEntity<JwtAuthResponse> signUp(@RequestBody @Valid UserSignUpDto request) {
        log.debug("REST request to register user: {}", request.getNickname());
        JwtAuthResponse response = authenticationService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Authenticate user")
    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthResponse> signIn(@RequestBody @Valid UserSignInDto request) {
        log.debug("REST request to authenticate user: {}", request.getLogin());
        JwtAuthResponse response = authenticationService.signIn(request);
        return ResponseEntity.ok(response);
    }
}

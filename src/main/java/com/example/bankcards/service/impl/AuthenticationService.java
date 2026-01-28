package com.example.bankcards.service.impl;

import com.example.bankcards.config.auth.JwtAuthResponse;
import com.example.bankcards.config.auth.JwtService;
import com.example.bankcards.dto.UserSignUpDto;
import com.example.bankcards.dto.UserSignInDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtAuthResponse signUp(UserSignUpDto request) {
        log.info("Registering new user: {}", request.getNickname());

        var user = User.builder()
                .username(request.getNickname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getNickname())
                .lastName(request.getNickname())
                .role(Role.USER)
                .build();

        userService.create(user);
        log.info("User registered successfully: {}", user.getUsername());

        var jwt = jwtService.generateToken(user);
        return new JwtAuthResponse(jwt);
    }

    public JwtAuthResponse signIn(UserSignInDto request) {
        log.info("Authenticating user: {}", request.getLogin());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()
                )
        );

        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getLogin());

        var jwt = jwtService.generateToken(user);
        log.info("User authenticated successfully: {}", request.getLogin());
        
        return new JwtAuthResponse(jwt);
    }
}
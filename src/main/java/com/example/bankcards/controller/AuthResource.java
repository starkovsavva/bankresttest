package com.example.bankcards.controller;

import com.example.bankcards.config.auth.JwtAuthResponse;
import com.example.bankcards.dto.UserSignInDto;
import com.example.bankcards.dto.UserSignUpDto;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.service.impl.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthResource {
    private final AuthenticationService authenticationService;

    /**
     * {@code POST  /api/auth/sign-up} : Регистрация нового пользователя.
     *
     * @param request the UserSignUpDto для регистрации.
     * @return the {@link JwtAuthResponse} с статусом {@code 200 (OK)} и с телом JwtAuthResponse,
     * содержащим JWT токен.
     * @throws BadRequestException если данные запроса невалидны или пользователь уже существует.
     * 
     */
    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public JwtAuthResponse signUp(@RequestBody @Valid UserSignUpDto request) throws UserAlreadyExistsException {
        System.out.println("signUp endpoint called with: " + request);
        return authenticationService.signUp(request);
    }

    /**
     * {@code POST  /api/auth/sign-in} : Авторизация пользователя.
     *
     * @param request the UserSignInDto для авторизации.
     * @return the {@link JwtAuthResponse} с статусом {@code 200 (OK)} и с телом JwtAuthResponse,
     * содержащим JWT токен.
     * @throws UnauthorizedException если учетные данные неверны.
     */
    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    public JwtAuthResponse signIn(@RequestBody @Valid UserSignInDto request) {
        return authenticationService.signIn(request);
    }



}

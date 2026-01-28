package com.example.bankcards.controller;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.config.auth.JwtAuthFilter;
import com.example.bankcards.config.auth.JwtAuthResponse;
import com.example.bankcards.config.security.SecurityConfig;
import com.example.bankcards.dto.UserSignInDto;
import com.example.bankcards.dto.UserSignUpDto;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.service.impl.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(value = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    @SuppressWarnings("unused")
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void signUp_shouldReturnJwtToken() throws Exception {
        JwtAuthResponse response = JwtAuthResponse.builder()
                .token("signup-token")
                .build();
        try {
            when(authenticationService.signUp(any(UserSignUpDto.class))).thenReturn(response);
        } catch (UserAlreadyExistsException e) {
            fail("Unexpected exception during stubbing", e);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("nickname", "newuser");
        payload.put("email", "newuser@example.com");
        payload.put("password", "Pa55word!");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signup-token"));

        ArgumentCaptor<UserSignUpDto> captor = ArgumentCaptor.forClass(UserSignUpDto.class);
        try {
            verify(authenticationService).signUp(captor.capture());
        } catch (UserAlreadyExistsException e) {
            fail("Unexpected exception during verification", e);
        }
        UserSignUpDto value = captor.getValue();
        assertThat(value.getNickname()).isEqualTo("newuser");
        assertThat(value.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    void signIn_shouldReturnJwtToken() throws Exception {
        JwtAuthResponse response = JwtAuthResponse.builder()
                .token("signin-token")
                .build();
        when(authenticationService.signIn(any(UserSignInDto.class))).thenReturn(response);

        Map<String, Object> payload = new HashMap<>();
        payload.put("login", "existinguser");
        payload.put("password", "Pa55word!");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signin-token"));

        ArgumentCaptor<UserSignInDto> captor = ArgumentCaptor.forClass(UserSignInDto.class);
        verify(authenticationService).signIn(captor.capture());
        UserSignInDto value = captor.getValue();
        assertThat(value.getLogin()).isEqualTo("existinguser");
    }
}

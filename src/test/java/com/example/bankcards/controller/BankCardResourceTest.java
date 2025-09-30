package com.example.bankcards.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.config.auth.JwtAuthFilter;
import com.example.bankcards.config.security.SecurityConfig;
import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.dto.requests.CardCreateRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.entity.BankCardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.BankCardService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(value = BankCardResource.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
class BankCardResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BankCardService bankCardService;

    @MockBean
    @SuppressWarnings("unused")
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(long id, Role role) {
        User user = User.builder()
                .id(id)
                .username(role == Role.ADMIN ? "admin" : "user")
                .email((role == Role.ADMIN ? "admin" : "user") + "@test.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private BankCardDTO buildCardDto() {
        return new BankCardDTO(
                1L,
                "1234567812345678",
                "John Doe",
                LocalDateTime.of(2030, 1, 1, 0, 0),
                BankCardStatus.ACTIVE,
                BigDecimal.valueOf(1000),
                "**** **** **** 5678",
                1L
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_shouldReturnCreatedCard() throws Exception {
    authenticateUser(1L, Role.ADMIN);
        BankCardDTO dto = buildCardDto();
        when(bankCardService.save(any(CardCreateRequest.class))).thenReturn(dto);

        CardCreateRequest request = new CardCreateRequest(
                "1234567812345678",
                "John Doe",
                LocalDateTime.of(2030, 1, 1, 0, 0),
                1L
        );

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/cards/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"));

        ArgumentCaptor<CardCreateRequest> requestCaptor = ArgumentCaptor.forClass(CardCreateRequest.class);
        verify(bankCardService).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().cardNumber()).isEqualTo("1234567812345678");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCard_shouldReturnUpdatedCard() throws Exception {
    authenticateUser(1L, Role.ADMIN);
        BankCardDTO dto = buildCardDto();
        when(bankCardService.update(any(BankCardDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/cards/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value(BankCardStatus.ACTIVE.name()));

        ArgumentCaptor<BankCardDTO> dtoCaptor = ArgumentCaptor.forClass(BankCardDTO.class);
        verify(bankCardService).update(dtoCaptor.capture());
        assertThat(dtoCaptor.getValue().id()).isEqualTo(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_shouldReturnPagedContent() throws Exception {
    authenticateUser(1L, Role.ADMIN);
        Page<BankCardDTO> page = new PageImpl<>(List.of(buildCardDto()), PageRequest.of(0, 20), 1);
        when(bankCardService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(header().string(HttpHeaders.LINK, containsString("rel=\"last\"")))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bankCardService).findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_shouldReturnCard() throws Exception {
    authenticateUser(1L, Role.USER);
        when(bankCardService.findOne(1L)).thenReturn(buildCardDto());

        mockMvc.perform(get("/api/cards/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"));

        verify(bankCardService).findOne(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_shouldReturnNoContent() throws Exception {
    authenticateUser(1L, Role.ADMIN);
        mockMvc.perform(delete("/api/cards/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(bankCardService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void transferBetweenCards_shouldReturnOk() throws Exception {
    authenticateUser(1L, Role.USER);
        when(bankCardService.transferBetweenOwnCards(any(TransferRequest.class), anyLong())).thenReturn(true);

        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<TransferRequest> requestCaptor = ArgumentCaptor.forClass(TransferRequest.class);
        ArgumentCaptor<Long> userCaptor = ArgumentCaptor.forClass(Long.class);
        verify(bankCardService).transferBetweenOwnCards(requestCaptor.capture(), userCaptor.capture());
        assertThat(requestCaptor.getValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(userCaptor.getValue()).isEqualTo(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void blockCard_shouldReturnUpdatedCard() throws Exception {
    authenticateUser(1L, Role.USER);
        BankCardDTO dto = buildCardDto();
        when(bankCardService.blockCard(1L, 1L)).thenReturn(dto);

        mockMvc.perform(put("/api/cards/{id}/block", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bankCardService).blockCard(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyCards_shouldReturnUserCards() throws Exception {
    authenticateUser(1L, Role.USER);
        Page<BankCardDTO> page = new PageImpl<>(List.of(buildCardDto()), PageRequest.of(0, 20), 1);
        when(bankCardService.findByUserId(anyLong(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/my-cards")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].id").value(1));

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(bankCardService).findByUserId(userIdCaptor.capture(), any(Pageable.class));
        assertThat(userIdCaptor.getValue()).isEqualTo(1L);
    }
}

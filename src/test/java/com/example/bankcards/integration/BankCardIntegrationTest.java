package com.example.bankcards.integration;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.dto.requests.CardCreateRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.entity.BankCardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.BankCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankCardIntegrationTest {

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        bankCardRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("integrationuser")
                .email("integration@test.com")
                .password("password")
                .firstName("Integration")
                .lastName("User")
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should create and find card")
    void createAndFindCard() {
        CardCreateRequest request = new CardCreateRequest(
                "4111111111111111",
                "Integration Test User",
                LocalDateTime.now().plusYears(3),
                testUser.getId()
        );

        BankCardDTO created = bankCardService.save(request);

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.cardHolderName()).isEqualTo("Integration Test User");

        BankCardDTO found = bankCardService.findOne(created.id());
        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(created.id());
    }

    @Test
    @DisplayName("Should find cards by user")
    void findCardsByUser() {
        CardCreateRequest request1 = new CardCreateRequest(
                "4111111111111111",
                "Card One",
                LocalDateTime.now().plusYears(3),
                testUser.getId()
        );
        CardCreateRequest request2 = new CardCreateRequest(
                "4222222222222222",
                "Card Two",
                LocalDateTime.now().plusYears(3),
                testUser.getId()
        );

        bankCardService.save(request1);
        bankCardService.save(request2);

        Page<BankCardDTO> cards = bankCardService.findByUserId(testUser.getId(), PageRequest.of(0, 10));

        assertThat(cards.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should transfer between cards")
    void transferBetweenCards() {
        CardCreateRequest request1 = new CardCreateRequest(
                "4111111111111111",
                "Source Card",
                LocalDateTime.now().plusYears(3),
                testUser.getId()
        );
        CardCreateRequest request2 = new CardCreateRequest(
                "4222222222222222",
                "Destination Card",
                LocalDateTime.now().plusYears(3),
                testUser.getId()
        );

        BankCardDTO sourceCard = bankCardService.save(request1);
        BankCardDTO destCard = bankCardService.save(request2);

        // Вручную добавляем баланс на карту-источник для теста
        var source = bankCardRepository.findById(sourceCard.id()).orElseThrow();
        source.setBalance(BigDecimal.valueOf(1000));
        bankCardRepository.save(source);

        TransferRequest transferRequest = new TransferRequest(
                sourceCard.id(),
                destCard.id(),
                BigDecimal.valueOf(300)
        );

        boolean result = bankCardService.transfer(transferRequest);

        assertThat(result).isTrue();

        BankCardDTO updatedSource = bankCardService.findOne(sourceCard.id());
        BankCardDTO updatedDest = bankCardService.findOne(destCard.id());

        assertThat(updatedSource.balance()).isEqualByComparingTo(BigDecimal.valueOf(700));
        assertThat(updatedDest.balance()).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    @DisplayName("Should block card")
    void blockCard() {
        CardCreateRequest request = new CardCreateRequest(
                "4111111111111111",
                "Card to Block",
                LocalDateTime.now().plusYears(3),
                testUser.getId()
        );

        BankCardDTO created = bankCardService.save(request);
        assertThat(created.status()).isEqualTo(BankCardStatus.ACTIVE);

        BankCardDTO blocked = bankCardService.blockCard(created.id(), testUser.getId());

        assertThat(blocked.status()).isEqualTo(BankCardStatus.BLOCKED);
    }

    @Test
    @DisplayName("Should delete card")
    void deleteCard() {
        CardCreateRequest request = new CardCreateRequest(
                "4111111111111111",
                "Card to Delete",
                LocalDateTime.now().plusYears(3),
                testUser.getId()
        );

        BankCardDTO created = bankCardService.save(request);
        assertThat(bankCardRepository.existsById(created.id())).isTrue();

        boolean deleted = bankCardService.delete(created.id());

        assertThat(deleted).isTrue();
        assertThat(bankCardRepository.existsById(created.id())).isFalse();
    }

    @Test
    @DisplayName("Should not allow transfer with insufficient funds")
    void transferInsufficientFunds() {
        CardCreateRequest request1 = new CardCreateRequest(
                "4111111111111111",
                "Source Card",
                LocalDateTime.now().plusYears(3),
                testUser.getId()
        );
        CardCreateRequest request2 = new CardCreateRequest(
                "4222222222222222",
                "Destination Card",
                LocalDateTime.now().plusYears(3),
                testUser.getId()
        );

        BankCardDTO sourceCard = bankCardService.save(request1);
        BankCardDTO destCard = bankCardService.save(request2);

        TransferRequest transferRequest = new TransferRequest(
                sourceCard.id(),
                destCard.id(),
                BigDecimal.valueOf(1000)
        );

        assertThatThrownBy(() -> bankCardService.transfer(transferRequest))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");
    }
}

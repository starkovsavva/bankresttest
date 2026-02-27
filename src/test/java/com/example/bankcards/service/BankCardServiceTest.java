package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.dto.requests.CardCreateRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.BankCardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.CardAccessDeniedException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.BankCardServiceImpl;
import com.example.bankcards.service.mapper.BankCardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankCardServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private BankCardMapper bankCardMapper;

    @InjectMocks
    private BankCardServiceImpl bankCardService;

    private User testUser;
    private BankCard testCard;
    private BankCardDTO testCardDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();

        testCard = BankCard.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .cardNumberHash("hashedCardNumber")
                .cardHolderName("Test User")
                .expirationDate(LocalDateTime.now().plusYears(2))
                .status(BankCardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .user(testUser)
                .build();

        testCardDTO = new BankCardDTO(
                1L,
                "1234567890123456",
                "Test User",
                LocalDateTime.now().plusYears(2),
                BankCardStatus.ACTIVE,
                BigDecimal.valueOf(1000),
                "**** **** **** 3456",
                1L
        );
    }

    @Nested
    @DisplayName("Create Card Tests")
    class CreateCardTests {

        @Test
        @DisplayName("Should create card successfully")
        void createCard_Success() {
            CardCreateRequest request = new CardCreateRequest(
                    "1234567890123456",
                    "Test User",
                    LocalDateTime.now().plusYears(2),
                    1L
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(encryptionService.hash(anyString())).thenReturn("hashedCardNumber");
            when(bankCardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);
            when(bankCardMapper.toDto(any(BankCard.class))).thenReturn(testCardDTO);

            BankCardDTO result = bankCardService.save(request);

            assertThat(result).isNotNull();
            assertThat(result.cardHolderName()).isEqualTo("Test User");
            verify(bankCardRepository).save(any(BankCard.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void createCard_UserNotFound() {
            CardCreateRequest request = new CardCreateRequest(
                    "1234567890123456",
                    "Test User",
                    LocalDateTime.now().plusYears(2),
                    999L
            );

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankCardService.save(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(bankCardRepository, never()).save(any(BankCard.class));
        }

        @Test
        @DisplayName("Should throw exception when card number already exists")
        void createCard_CardNumberExists() {
            CardCreateRequest request = new CardCreateRequest(
                    "1234567890123456",
                    "Test User",
                    LocalDateTime.now().plusYears(2),
                    1L
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(encryptionService.hash(anyString())).thenReturn("hashedCardNumber");
            when(bankCardRepository.existsByCardNumberHash(anyString())).thenReturn(true);

            assertThatThrownBy(() -> bankCardService.save(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Card number already exists");

            verify(bankCardRepository, never()).save(any(BankCard.class));
        }

        @Test
        @DisplayName("Should throw exception when expiration date is in the past")
        void createCard_ExpiredDate() {
            CardCreateRequest request = new CardCreateRequest(
                    "1234567890123456",
                    "Test User",
                    LocalDateTime.now().minusDays(1),
                    1L
            );

            assertThatThrownBy(() -> bankCardService.save(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("expiration date cannot be in the past");
        }
    }

    @Nested
    @DisplayName("Find Card Tests")
    class FindCardTests {

        @Test
        @DisplayName("Should find all cards with pagination")
        void findAll_Success() {
            Page<BankCard> cardPage = new PageImpl<>(List.of(testCard));
            Page<BankCardDTO> dtoPage = new PageImpl<>(List.of(testCardDTO));
            Pageable pageable = PageRequest.of(0, 10);

            when(bankCardRepository.findAll(pageable)).thenReturn(cardPage);
            when(bankCardMapper.toDtoSummaryPage(cardPage)).thenReturn(dtoPage);

            Page<BankCardDTO> result = bankCardService.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(bankCardRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should find card by id")
        void findOne_Success() {
            when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
            when(bankCardMapper.toDto(testCard)).thenReturn(testCardDTO);

            BankCardDTO result = bankCardService.findOne(1L);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when card not found")
        void findOne_NotFound() {
            when(bankCardRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankCardService.findOne(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Card not found");
        }

        @Test
        @DisplayName("Should find cards by user id")
        void findByUserId_Success() {
            Page<BankCard> cardPage = new PageImpl<>(List.of(testCard));
            Page<BankCardDTO> dtoPage = new PageImpl<>(List.of(testCardDTO));
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.existsById(1L)).thenReturn(true);
            when(bankCardRepository.findByUserId(1L, pageable)).thenReturn(cardPage);
            when(bankCardMapper.toDtoSummaryPage(cardPage)).thenReturn(dtoPage);

            Page<BankCardDTO> result = bankCardService.findByUserId(1L, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Transfer Tests")
    class TransferTests {

        private BankCard sourceCard;
        private BankCard destinationCard;

        @BeforeEach
        void setUpTransfer() {
            sourceCard = BankCard.builder()
                    .id(1L)
                    .cardNumber("1111111111111111")
                    .cardNumberHash("hash1")
                    .cardHolderName("Source User")
                    .expirationDate(LocalDateTime.now().plusYears(2))
                    .status(BankCardStatus.ACTIVE)
                    .balance(BigDecimal.valueOf(1000))
                    .user(testUser)
                    .build();

            destinationCard = BankCard.builder()
                    .id(2L)
                    .cardNumber("2222222222222222")
                    .cardNumberHash("hash2")
                    .cardHolderName("Destination User")
                    .expirationDate(LocalDateTime.now().plusYears(2))
                    .status(BankCardStatus.ACTIVE)
                    .balance(BigDecimal.valueOf(500))
                    .user(testUser)
                    .build();
        }

        @Test
        @DisplayName("Should transfer successfully between cards")
        void transfer_Success() {
            TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100));

            when(bankCardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
            when(bankCardRepository.findById(2L)).thenReturn(Optional.of(destinationCard));
            when(bankCardRepository.saveAll(any(List.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            boolean result = bankCardService.transfer(request);

            assertThat(result).isTrue();
            assertThat(sourceCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(900));
            assertThat(destinationCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(600));
        }

        @Test
        @DisplayName("Should throw exception when transferring to same card")
        void transfer_SameCard() {
            TransferRequest request = new TransferRequest(1L, 1L, BigDecimal.valueOf(100));

            assertThatThrownBy(() -> bankCardService.transfer(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot transfer to the same card");
        }

        @Test
        @DisplayName("Should throw exception when source card not found")
        void transfer_SourceNotFound() {
            TransferRequest request = new TransferRequest(999L, 2L, BigDecimal.valueOf(100));

            when(bankCardRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankCardService.transfer(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Source card");
        }

        @Test
        @DisplayName("Should throw exception when insufficient funds")
        void transfer_InsufficientFunds() {
            TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(2000));

            when(bankCardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
            when(bankCardRepository.findById(2L)).thenReturn(Optional.of(destinationCard));

            assertThatThrownBy(() -> bankCardService.transfer(request))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("Should throw exception when source card is blocked")
        void transfer_SourceBlocked() {
            sourceCard.setStatus(BankCardStatus.BLOCKED);
            TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100));

            when(bankCardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
            when(bankCardRepository.findById(2L)).thenReturn(Optional.of(destinationCard));

            assertThatThrownBy(() -> bankCardService.transfer(request))
                    .isInstanceOf(CardOperationException.class)
                    .hasMessageContaining("Source card is not active");
        }

        @Test
        @DisplayName("Should transfer between own cards successfully")
        void transferBetweenOwnCards_Success() {
            TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100));

            when(bankCardRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
            when(bankCardRepository.existsByIdAndUserId(2L, 1L)).thenReturn(true);
            when(bankCardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
            when(bankCardRepository.findById(2L)).thenReturn(Optional.of(destinationCard));
            when(bankCardRepository.saveAll(any(List.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            boolean result = bankCardService.transferBetweenOwnCards(request, 1L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when cards don't belong to user")
        void transferBetweenOwnCards_NotOwnCards() {
            TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100));

            when(bankCardRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
            when(bankCardRepository.existsByIdAndUserId(2L, 1L)).thenReturn(false);

            assertThatThrownBy(() -> bankCardService.transferBetweenOwnCards(request, 1L))
                    .isInstanceOf(CardAccessDeniedException.class)
                    .hasMessageContaining("do not belong to the user");
        }
    }

    @Nested
    @DisplayName("Block Card Tests")
    class BlockCardTests {

        @Test
        @DisplayName("Should block card successfully")
        void blockCard_Success() {
            when(bankCardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCard));
            when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);
            when(bankCardMapper.toDto(any(BankCard.class))).thenReturn(testCardDTO);

            BankCardDTO result = bankCardService.blockCard(1L, 1L);

            assertThat(result).isNotNull();

            ArgumentCaptor<BankCard> captor = ArgumentCaptor.forClass(BankCard.class);
            verify(bankCardRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(BankCardStatus.BLOCKED);
        }

        @Test
        @DisplayName("Should throw exception when card not found or access denied")
        void blockCard_NotFoundOrAccessDenied() {
            when(bankCardRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankCardService.blockCard(1L, 2L))
                    .isInstanceOf(CardAccessDeniedException.class)
                    .hasMessageContaining("Card not found or access denied");
        }
    }

    @Nested
    @DisplayName("Delete Card Tests")
    class DeleteCardTests {

        @Test
        @DisplayName("Should delete card successfully")
        void deleteCard_Success() {
            when(bankCardRepository.existsById(1L)).thenReturn(true);

            boolean result = bankCardService.delete(1L);

            assertThat(result).isTrue();
            verify(bankCardRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when card not found")
        void deleteCard_NotFound() {
            when(bankCardRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> bankCardService.delete(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Card not found");

            verify(bankCardRepository, never()).deleteById(anyLong());
        }
    }
}

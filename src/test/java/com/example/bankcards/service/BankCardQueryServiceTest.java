package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.BankCardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.service.criteria.BankCardCriteria;
import com.example.bankcards.service.criteria.Filter;
import com.example.bankcards.service.impl.BankCardQBServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankCardQueryServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private BankCardMapper bankCardMapper;

    @InjectMocks
    private BankCardQBServiceImpl queryService;

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
                null,
                "Test User",
                LocalDateTime.now().plusYears(2),
                BankCardStatus.ACTIVE,
                BigDecimal.valueOf(1000),
                "**** **** **** 3456",
                1L
        );
    }

    @Nested
    @DisplayName("Find by Criteria Tests")
    class FindByCriteriaTests {

        @Test
        @DisplayName("Should find cards with empty criteria")
        void findByCriteria_EmptyCriteria() {
            BankCardCriteria criteria = new BankCardCriteria();
            Pageable pageable = PageRequest.of(0, 10);
            Page<BankCard> cardPage = new PageImpl<>(List.of(testCard));

            when(bankCardRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(cardPage);
            when(bankCardMapper.toDtoSummary(any(BankCard.class))).thenReturn(testCardDTO);

            Page<BankCardDTO> result = queryService.findByCriteria(criteria, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should find cards by status")
        void findByCriteria_ByStatus() {
            BankCardCriteria criteria = new BankCardCriteria();
            criteria.setStatus(Filter.eq(BankCardStatus.ACTIVE));
            Pageable pageable = PageRequest.of(0, 10);
            Page<BankCard> cardPage = new PageImpl<>(List.of(testCard));

            when(bankCardRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(cardPage);
            when(bankCardMapper.toDtoSummary(any(BankCard.class))).thenReturn(testCardDTO);

            Page<BankCardDTO> result = queryService.findByCriteria(criteria, pageable);

            assertThat(result).isNotNull();
            verify(bankCardRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should find cards by card holder name containing")
        void findByCriteria_ByCardHolderNameContains() {
            BankCardCriteria criteria = new BankCardCriteria();
            criteria.setCardHolderName(Filter.contains("Test"));
            Pageable pageable = PageRequest.of(0, 10);
            Page<BankCard> cardPage = new PageImpl<>(List.of(testCard));

            when(bankCardRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(cardPage);
            when(bankCardMapper.toDtoSummary(any(BankCard.class))).thenReturn(testCardDTO);

            Page<BankCardDTO> result = queryService.findByCriteria(criteria, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should find cards by balance range")
        void findByCriteria_ByBalanceRange() {
            BankCardCriteria criteria = new BankCardCriteria();
            Filter<BigDecimal> balanceFilter = new Filter<>();
            balanceFilter.setGreaterOrEqual(BigDecimal.valueOf(500));
            balanceFilter.setLessOrEqual(BigDecimal.valueOf(2000));
            criteria.setBalance(balanceFilter);
            Pageable pageable = PageRequest.of(0, 10);
            Page<BankCard> cardPage = new PageImpl<>(List.of(testCard));

            when(bankCardRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(cardPage);
            when(bankCardMapper.toDtoSummary(any(BankCard.class))).thenReturn(testCardDTO);

            Page<BankCardDTO> result = queryService.findByCriteria(criteria, pageable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find cards by user id")
        void findByCriteria_ByUserId() {
            BankCardCriteria criteria = new BankCardCriteria();
            criteria.setUserId(Filter.eq(1L));
            Pageable pageable = PageRequest.of(0, 10);
            Page<BankCard> cardPage = new PageImpl<>(List.of(testCard));

            when(bankCardRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(cardPage);
            when(bankCardMapper.toDtoSummary(any(BankCard.class))).thenReturn(testCardDTO);

            Page<BankCardDTO> result = queryService.findByCriteria(criteria, pageable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find cards with multiple criteria")
        void findByCriteria_MultipleCriteria() {
            BankCardCriteria criteria = new BankCardCriteria();
            criteria.setStatus(Filter.eq(BankCardStatus.ACTIVE));
            criteria.setCardHolderName(Filter.contains("Test"));
            criteria.setUserId(Filter.eq(1L));
            Pageable pageable = PageRequest.of(0, 10);
            Page<BankCard> cardPage = new PageImpl<>(List.of(testCard));

            when(bankCardRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(cardPage);
            when(bankCardMapper.toDtoSummary(any(BankCard.class))).thenReturn(testCardDTO);

            Page<BankCardDTO> result = queryService.findByCriteria(criteria, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty page when no matches")
        void findByCriteria_NoMatches() {
            BankCardCriteria criteria = new BankCardCriteria();
            criteria.setStatus(Filter.eq(BankCardStatus.EXPIRED));
            Pageable pageable = PageRequest.of(0, 10);
            Page<BankCard> emptyPage = new PageImpl<>(List.of());

            when(bankCardRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            Page<BankCardDTO> result = queryService.findByCriteria(criteria, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Count by Criteria Tests")
    class CountByCriteriaTests {

        @Test
        @DisplayName("Should count cards by criteria")
        void countByCriteria_Success() {
            BankCardCriteria criteria = new BankCardCriteria();
            criteria.setStatus(Filter.eq(BankCardStatus.ACTIVE));

            when(bankCardRepository.count(any(Specification.class))).thenReturn(5L);

            long count = queryService.countByCriteria(criteria);

            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should return zero when no matches")
        void countByCriteria_NoMatches() {
            BankCardCriteria criteria = new BankCardCriteria();
            criteria.setStatus(Filter.eq(BankCardStatus.EXPIRED));

            when(bankCardRepository.count(any(Specification.class))).thenReturn(0L);

            long count = queryService.countByCriteria(criteria);

            assertThat(count).isZero();
        }
    }
}

package com.example.bankcards.service.impl;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.dto.requests.CardCreateRequest;
import com.example.bankcards.dto.requests.CardSearchRequest;
import com.example.bankcards.dto.requests.CardUpdateRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.BankCardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.EncryptionService;
import com.example.bankcards.service.mapper.BankCardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BankCardServiceImpl implements BankCardService {

    private final BankCardRepository bankCardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final BankCardMapper bankCardMapper;
//save
    @Override
    public BankCardDTO save(CardCreateRequest request) {
        log.info("Creating new card for user with request: {}", request);

        // Валидация срока действия карты
        if (request.expirationDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Card expiration date cannot be in the past");
        }

        User user = userRepository.findById(request.targetUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.targetUserId()));

        // Проверка уникальности номера карты
        String cardHash = encryptionService.hash(request.cardNumber());
        if (bankCardRepository.existsByCardNumberHash(cardHash)) {
            throw new RuntimeException("Card number already exists");
        }

        BankCard card = BankCard.builder()
                .cardNumber(request.cardNumber())
                .cardNumberHash(cardHash)
                .cardHolderName(request.cardHolderName())
                .expirationDate(request.expirationDate())
                .status(BankCardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        BankCard savedCard = bankCardRepository.save(card);
        log.info("Card created successfully with id: {}", savedCard.getId());

        return bankCardMapper.toDto(savedCard);

    }
//update
    @Override
    public BankCardDTO update(BankCardDTO bankCardDTO) {
        log.info("Updating card with id: {}", bankCardDTO.id());

        BankCard card = bankCardRepository.findById(bankCardDTO.id())
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + bankCardDTO.id()));

        // Обновляем только разрешенные поля
        card.setCardHolderName(bankCardDTO.cardHolderName());
        card.setExpirationDate(bankCardDTO.expirationDate());
        card.setStatus(bankCardDTO.status());
        card.setBalance(bankCardDTO.balance());

        BankCard updatedCard = bankCardRepository.save(card);

        return bankCardMapper.toDto(updatedCard);
    }

    @Override
    public BankCardDTO updateStatus(Long cardId, CardUpdateRequest request) {
        log.info("Updating status for card id: {} to status: {}", cardId, request.status());

        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        card.setStatus(request.status());
        BankCard updatedCard = bankCardRepository.save(card);

        return bankCardMapper.toDto(updatedCard);
    }
//findAll
    @Override
    @Transactional(readOnly = true)
    public Page<BankCardDTO> findAll(Pageable pageable) {
        log.debug("Finding all cards with pagination: {}", pageable);

        return bankCardMapper.toDtoSummaryPage(bankCardRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankCardDTO> findByUserId(Long userId, Pageable pageable) {
        log.debug("Finding cards for user id: {} with pagination: {}", userId, pageable);

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        return bankCardMapper.toDtoSummaryPage(bankCardRepository.findByUserId(userId, pageable));
    }


//    @Override
//    @Transactional(readOnly = true)
//    public Page<BankCardDTO> searchCards(CardSearchRequest searchRequest) {
//        log.debug("Searching cards with criteria: {}", searchRequest);
//
//        Specification<BankCard> spec = createSearchSpecification(searchRequest);
//        Pageable pageable = Pageable.ofSize(searchRequest.size()).withPage(searchRequest.page());
//
//        return bankCardMapper.toDtoSummaryPage(bankCardRepository.findAll(spec, pageable));
//    }

    @Override
    @Transactional(readOnly = true)
    public BankCardDTO findOne(Long id) {
        log.debug("Finding card by id: {}", id);

        BankCard card = bankCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));

        return bankCardMapper.toDto(card);
    }

//    @Override
//    public BankCardDTO findByIdAndUserId(Long id, Long userId) {
//        return null;
//    }


    @Override
    @Transactional
    public boolean transferBetweenOwnCards(TransferRequest request, Long userId){
        log.info("Processing transfer between own cards for user {}: {}", userId, request);

        // Быстрая валидация
        if (request.fromCardId().equals(request.toCardId())) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }

        // Атомарная проверка принадлежности карт пользователю
        boolean cardsBelongToUser = bankCardRepository.existsByIdAndUserId(request.fromCardId(), userId)
                && bankCardRepository.existsByIdAndUserId(request.toCardId(), userId);

        if (!cardsBelongToUser) {
            throw new SecurityException("One or both cards do not belong to the user");
        }

        transfer(request);

        return true;
    };
    @Override
    @Transactional
    public boolean transfer(TransferRequest request) {
        log.info("Processing transfer request: {}", request);

        if (request.fromCardId().equals(request.toCardId())) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }

        BankCard fromCard = bankCardRepository.findById(request.fromCardId())
                .orElseThrow(() -> new RuntimeException("Source card not found"));

        BankCard toCard = bankCardRepository.findById(request.toCardId())
                .orElseThrow(() -> new RuntimeException("Destination card not found"));

        // Проверка возможности перевода
        validateTransfer(fromCard, toCard, request.amount());

        // Выполнение перевода
        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        toCard.setBalance(toCard.getBalance().add(request.amount()));

        bankCardRepository.save(fromCard);
        bankCardRepository.save(toCard);

        log.info("Transfer completed successfully from card {} to card {}",
                request.fromCardId(), request.toCardId());

        return true;
    }

//    @Override
//    @Transactional(readOnly = true)
//    public BigDecimal getBalance(Long cardId) {
//        log.debug("Getting balance for card id: {}", cardId);
//
//        BankCard card = bankCardRepository.findById(cardId)
//                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));
//
//        return card.getBalance();
//    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        log.info("Deleting card with id: {}", id);

        if (!bankCardRepository.existsById(id)) {
            throw new RuntimeException("Card not found with id: " + id);
        }

        bankCardRepository.deleteById(id);
        log.info("Card deleted successfully with id: {}", id);
        return true;
    }

    @Override
    public BankCardDTO blockCard(Long cardId, Long userId) {
        log.info("Blocking card id: {} for user id: {}", cardId, userId);

        BankCard card = bankCardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new RuntimeException("Card not found or access denied"));

        card.setStatus(BankCardStatus.BLOCKED);
        BankCard updatedCard = bankCardRepository.save(card);

        return bankCardMapper.toDto(updatedCard);
    }



    private void validateTransfer(BankCard fromCard, BankCard toCard, BigDecimal amount) {
        // Проверка статуса карт
        if (fromCard.getStatus() != BankCardStatus.ACTIVE) {
            throw new RuntimeException("Source card is not active");
        }

        if (toCard.getStatus() != BankCardStatus.ACTIVE) {
            throw new RuntimeException("Destination card is not active");
        }

        // Проверка срока действия
        if (fromCard.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Source card has expired");
        }

        if (toCard.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Destination card has expired");
        }

        // Проверка достаточности средств
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // Проверка положительной суммы
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }
    }


}
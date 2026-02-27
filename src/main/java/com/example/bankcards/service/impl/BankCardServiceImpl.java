package com.example.bankcards.service.impl;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.dto.requests.CardCreateRequest;
import com.example.bankcards.dto.requests.CardUpdateRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.BankCardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.CardAccessDeniedException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.EncryptionService;
import com.example.bankcards.service.mapper.BankCardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BankCardServiceImpl implements BankCardService {

    private final BankCardRepository bankCardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final BankCardMapper bankCardMapper;

    @Override
    public BankCardDTO save(CardCreateRequest request) {
        log.info("Creating new card for user id: {}", request.targetUserId());

        if (request.expirationDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Card expiration date cannot be in the past");
        }

        User user = userRepository.findById(request.targetUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.targetUserId()));

        String cardHash = encryptionService.hash(request.cardNumber());
        if (bankCardRepository.existsByCardNumberHash(cardHash)) {
            throw new BadRequestException("Card number already exists");
        }

        String encryptedCardNumber = encryptionService.encrypt(request.cardNumber());

        BankCard card = BankCard.builder()
                .cardNumber(encryptedCardNumber)
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

    @Override
    public BankCardDTO update(BankCardDTO bankCardDTO) {
        log.info("Updating card with id: {}", bankCardDTO.id());

        BankCard card = bankCardRepository.findById(bankCardDTO.id())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", bankCardDTO.id()));

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
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId));

        card.setStatus(request.status());
        BankCard updatedCard = bankCardRepository.save(card);

        return bankCardMapper.toDto(updatedCard);
    }

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
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        return bankCardMapper.toDtoSummaryPage(bankCardRepository.findByUserId(userId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public BankCardDTO findOne(Long id) {
        log.debug("Finding card by id: {}", id);

        BankCard card = bankCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));

        return bankCardMapper.toDto(card);
    }

    @Override
    @Transactional(readOnly = true)
    public BankCardDTO findOneForUser(Long id, Long userId) {
        log.debug("Finding card by id: {} for user id: {}", id, userId);

        BankCard card = bankCardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CardAccessDeniedException("Card not found or access denied"));

        return bankCardMapper.toDto(card);
    }

    @Override
    @Transactional
    public boolean transferBetweenOwnCards(TransferRequest request, Long userId) {
        log.info("Processing transfer between own cards for user {}: fromCard={}, toCard={}, amount={}",
                userId, request.fromCardId(), request.toCardId(), request.amount());

        if (request.fromCardId().equals(request.toCardId())) {
            throw new BadRequestException("Cannot transfer to the same card");
        }

        boolean cardsBelongToUser = bankCardRepository.existsByIdAndUserId(request.fromCardId(), userId)
                && bankCardRepository.existsByIdAndUserId(request.toCardId(), userId);

        if (!cardsBelongToUser) {
            throw new CardAccessDeniedException("One or both cards do not belong to the user");
        }

        transfer(request);

        return true;
    }

    @Override
    @Transactional
    public boolean transfer(TransferRequest request) {
        log.info("Processing transfer: fromCard={}, toCard={}, amount={}",
                request.fromCardId(), request.toCardId(), request.amount());

        if (request.fromCardId().equals(request.toCardId())) {
            throw new BadRequestException("Cannot transfer to the same card");
        }

        BankCard fromCard = bankCardRepository.findById(request.fromCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Source card", "id", request.fromCardId()));

        BankCard toCard = bankCardRepository.findById(request.toCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination card", "id", request.toCardId()));

        validateTransfer(fromCard, toCard, request.amount());

        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        toCard.setBalance(toCard.getBalance().add(request.amount()));

        bankCardRepository.saveAll(List.of(fromCard, toCard));

        log.info("Transfer completed successfully from card {} to card {}",
                request.fromCardId(), request.toCardId());

        return true;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        log.info("Deleting card with id: {}", id);

        if (!bankCardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Card", "id", id);
        }

        bankCardRepository.deleteById(id);
        log.info("Card deleted successfully with id: {}", id);
        return true;
    }

    @Override
    public BankCardDTO blockCard(Long cardId, Long userId) {
        log.info("Blocking card id: {} for user id: {}", cardId, userId);

        BankCard card = bankCardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CardAccessDeniedException("Card not found or access denied"));

        card.setStatus(BankCardStatus.BLOCKED);
        BankCard updatedCard = bankCardRepository.save(card);

        return bankCardMapper.toDto(updatedCard);
    }

    private void validateTransfer(BankCard fromCard, BankCard toCard, BigDecimal amount) {
        if (fromCard.getStatus() != BankCardStatus.ACTIVE) {
            throw new CardOperationException("Source card is not active");
        }

        if (toCard.getStatus() != BankCardStatus.ACTIVE) {
            throw new CardOperationException("Destination card is not active");
        }

        if (fromCard.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new CardOperationException("Source card has expired");
        }

        if (toCard.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new CardOperationException("Destination card has expired");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be positive");
        }
    }
}

package com.example.bankcards.service.impl;

import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    @Override
    public BankCardDTO save(CardCreateRequest request) {
        log.info("Creating new card for user with request: {}", request);

        // Валидация срока действия карты
        if (request.expirationDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Card expiration date cannot be in the past");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.userId()));

        // Проверка уникальности номера карты
        String cardHash = encryptionService.hash(request.cardNumber());
        if (cardRepository.existsByCardNumberHash(cardHash)) {
            throw new RuntimeException("Card number already exists");
        }

        BankCard card = BankCard.builder()
                .cardNumber(encryptionService.encrypt(request.cardNumber()))
                .cardNumberHash(cardHash)
                .cardHolderName(request.cardHolderName())
                .expirationDate(request.expirationDate())
                .status(BankCardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        BankCard savedCard = cardRepository.save(card);
        log.info("Card created successfully with id: {}", savedCard.getId());

        return BankCardDTO.withMaskedNumber(
                savedCard.getId(),
                encryptionService.maskCardNumber(request.cardNumber()),
                savedCard.getCardHolderName(),
                savedCard.getExpirationDate(),
                savedCard.getStatus(),
                savedCard.getBalance(),
                savedCard.getUser().getId()
        );
    }

    @Override
    public BankCardDTO update(BankCardDTO bankCardDTO) {
        log.info("Updating card with id: {}", bankCardDTO.id());

        BankCard card = cardRepository.findById(bankCardDTO.id())
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + bankCardDTO.id()));

        // Обновляем только разрешенные поля
        card.setCardHolderName(bankCardDTO.cardHolderName());
        card.setExpirationDate(bankCardDTO.expirationDate());
        card.setStatus(bankCardDTO.status());
        card.setBalance(bankCardDTO.balance());

        BankCard updatedCard = cardRepository.save(card);

        return mapToDTO(updatedCard);
    }

    @Override
    public BankCardDTO updateStatus(Long cardId, CardUpdateRequest request) {
        log.info("Updating status for card id: {} to status: {}", cardId, request.status());

        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        card.setStatus(request.status());
        BankCard updatedCard = cardRepository.save(card);

        return mapToDTO(updatedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankCardDTO> findAll(Pageable pageable) {
        log.debug("Finding all cards with pagination: {}", pageable);

        return cardRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankCardDTO> findByUserId(Long userId, Pageable pageable) {
        log.debug("Finding cards for user id: {} with pagination: {}", userId, pageable);

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        return cardRepository.findByUserId(userId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankCardDTO> searchCards(CardSearchRequest searchRequest) {
        log.debug("Searching cards with criteria: {}", searchRequest);

        Specification<BankCard> spec = createSearchSpecification(searchRequest);
        Pageable pageable = Pageable.ofSize(searchRequest.size()).withPage(searchRequest.page());

        return cardRepository.findAll(spec, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public BankCardDTO findOne(Long id) {
        log.debug("Finding card by id: {}", id);

        BankCard card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));

        return mapToDTO(card);
    }

    @Override
    @Transactional(readOnly = true)
    public BankCardDTO findByIdAndUserId(Long id, Long userId) {
        log.debug("Finding card by id: {} for user id: {}", id, userId);

        BankCard card = cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id + " for user: " + userId));

        return mapToDTO(card);
    }

    @Override
    @Transactional
    public void transfer(TransferRequest request) {
        log.info("Processing transfer request: {}", request);

        if (request.fromCardId().equals(request.toCardId())) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }

        BankCard fromCard = cardRepository.findById(request.fromCardId())
                .orElseThrow(() -> new RuntimeException("Source card not found"));

        BankCard toCard = cardRepository.findById(request.toCardId())
                .orElseThrow(() -> new RuntimeException("Destination card not found"));

        // Проверка возможности перевода
        validateTransfer(fromCard, toCard, request.amount());

        // Выполнение перевода
        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        toCard.setBalance(toCard.getBalance().add(request.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Transfer completed successfully from card {} to card {}",
                request.fromCardId(), request.toCardId());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long cardId) {
        log.debug("Getting balance for card id: {}", cardId);

        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        return card.getBalance();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting card with id: {}", id);

        if (!cardRepository.existsById(id)) {
            throw new RuntimeException("Card not found with id: " + id);
        }

        cardRepository.deleteById(id);
        log.info("Card deleted successfully with id: {}", id);
    }

    @Override
    public BankCardDTO blockCard(Long cardId, Long userId) {
        log.info("Blocking card id: {} for user id: {}", cardId, userId);

        BankCard card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new RuntimeException("Card not found or access denied"));

        card.setStatus(BankCardStatus.BLOCKED);
        BankCard updatedCard = cardRepository.save(card);

        return mapToDTO(updatedCard);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private Specification<BankCard> createSearchSpecification(CardSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.cardHolderName() != null && !request.cardHolderName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("cardHolderName")),
                        "%" + request.cardHolderName().toLowerCase() + "%"
                ));
            }

            if (request.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.status()));
            }

            if (request.expirationDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("expirationDate"), request.expirationDateFrom()
                ));
            }

            if (request.expirationDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("expirationDate"), request.expirationDateTo()
                ));
            }

            // Поиск по имени пользователя (если нужно)
            if (request.userName() != null && !request.userName().isBlank()) {
                Join<BankCard, User> userJoin = root.join("user");
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("firstName")),
                        "%" + request.userName().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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
        if (fromCard.getExpirationDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Source card has expired");
        }

        if (toCard.getExpirationDate().isBefore(LocalDate.now())) {
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

    private BankCardDTO mapToDTO(BankCard card) {
        try {
            // Дешифруем номер карты для маскирования
            String decryptedNumber = encryptionService.decrypt(card.getCardNumber());
            String maskedNumber = encryptionService.maskCardNumber(decryptedNumber);

            return BankCardDTO.withMaskedNumber(
                    card.getId(),
                    maskedNumber,
                    card.getCardHolderName(),
                    card.getExpirationDate(),
                    card.getStatus(),
                    card.getBalance(),
                    card.getUser().getId()
            );
        } catch (Exception e) {
            log.warn("Error decrypting card number for card id: {}, using fallback masking", card.getId());
            return BankCardDTO.withMaskedNumber(
                    card.getId(),
                    "**** **** **** ****",
                    card.getCardHolderName(),
                    card.getExpirationDate(),
                    card.getStatus(),
                    card.getBalance(),
                    card.getUser().getId()
            );
        }
    }
}
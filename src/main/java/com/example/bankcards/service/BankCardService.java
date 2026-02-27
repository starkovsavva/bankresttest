package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.dto.requests.CardCreateRequest;
import com.example.bankcards.dto.requests.CardUpdateRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BankCardService {
    BankCardDTO save(CardCreateRequest request);
    BankCardDTO update(BankCardDTO bankCardDTO);
    BankCardDTO updateStatus(Long cardId, CardUpdateRequest request);
    Page<BankCardDTO> findAll(Pageable pageable);
    Page<BankCardDTO> findByUserId(Long userId, Pageable pageable);
    BankCardDTO findOne(Long id);
    BankCardDTO findOneForUser(Long id, Long userId);
    boolean transferBetweenOwnCards(TransferRequest request, Long userId);
    boolean transfer(TransferRequest request);
    boolean delete(Long id);
    BankCardDTO blockCard(Long cardId, Long userId);
}
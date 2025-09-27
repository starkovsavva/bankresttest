package com.example.bankcards.service;


import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.dto.requests.CardSearchRequest;
import com.example.bankcards.dto.requests.CardCreateRequest;
import com.example.bankcards.dto.requests.CardUpdateRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface BankCardService {
    BankCardDTO save(CardCreateRequest request);
    BankCardDTO update(BankCardDTO BankcardDTO);
    BankCardDTO updateStatus(Long cardId, CardUpdateRequest request);
    Page<BankCardDTO> findAll(Pageable pageable);
    Page<BankCardDTO> findByUserId(Long userId, Pageable pageable);
    Page<BankCardDTO> searchCards(CardSearchRequest searchRequest);
    BankCardDTO findOne(Long id);
    BankCardDTO findByIdAndUserId(Long id, Long userId);
    void transfer(TransferRequest request);
    BigDecimal getBalance(Long cardId);
    void delete(Long id);
    BankCardDTO blockCard(Long cardId, Long userId);
}
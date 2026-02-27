package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard,Long>, JpaSpecificationExecutor<BankCard> {

    boolean existsByCardNumberHash(String cardHash);

    Page<BankCard> findByUserId(Long userId, Pageable pageable);

    Optional<BankCard> findByIdAndUserId(Long cardId, Long userId);

    boolean existsByIdAndUserId(Long cardId, Long userId);
}

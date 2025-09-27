package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard,Long>, JpaSpecificationExecutor<BankCard> {

    boolean existsByCardNumberHash(String cardHash);

    List<BankCard> findAllWithUserByIds(List<Long> bankCardIds);

    Optional<Object> findByUserId(Long userId, Pageable pageable);
}

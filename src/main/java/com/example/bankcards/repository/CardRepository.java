package com.example.bankcards.repository;

import com.example.bankcards.dto.BankCardDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<BankCardDTO,Long> {
}

package com.example.bankcards.service.mapper;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.service.criteria.BankCardCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Query Builder Service for BankCard entities.
 */
public interface BankCardQBService {

    /**
     * Find bank cards by criteria with pagination.
     *
     * @param criteria The criteria which holds all the filters.
     * @param pageable The pagination information.
     * @return the list of matching BankCardDTOs.
     */
    Page<BankCardDTO> findByCriteria(BankCardCriteria criteria, Pageable pageable);
}
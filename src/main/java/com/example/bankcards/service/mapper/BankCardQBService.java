package com.example.bankcards.service.mapper;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.service.criteria.BankCardCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис построения запросов для сущностей BankCard.
 */
public interface BankCardQBService {

    /**
     * Поиск банковских карт по критериям с пагинацией.
     *
     * @param criteria критерии, содержащие все фильтры.
     * @param pageable информация о пагинации.
     * @return страница подходящих BankCardDTO.
     */
    Page<BankCardDTO> findByCriteria(BankCardCriteria criteria, Pageable pageable);
}
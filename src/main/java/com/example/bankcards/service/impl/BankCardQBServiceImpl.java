package com.example.bankcards.service.impl;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.BankCard_;
import com.example.bankcards.entity.User_;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.service.criteria.BankCardCriteria;
import com.example.bankcards.service.criteria.QueryBuilderService;
import com.example.bankcards.service.mapper.BankCardMapper;
import com.example.bankcards.service.mapper.BankCardQBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.JoinType;

/**
 * Сервис для выполнения сложных запросов по сущностям BankCard с использованием Criteria API.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class BankCardQBServiceImpl extends QueryBuilderService<BankCard> implements BankCardQBService {

    private final BankCardRepository bankCardRepository;
    private final BankCardMapper bankCardMapper;

    public BankCardQBServiceImpl(BankCardRepository bankCardRepository, BankCardMapper bankCardMapper) {
        this.bankCardRepository = bankCardRepository;
        this.bankCardMapper = bankCardMapper;
    }

    @Override
    public Page<BankCardDTO> findByCriteria(BankCardCriteria criteria, org.springframework.data.domain.Pageable pageable) {
        log.debug("Find by criteria: {}, pageable: {}", criteria, pageable);
        
        Specification<BankCard> specification = createSpecification(criteria);
        Page<BankCard> page = bankCardRepository.findAll(specification, pageable);
        
        return page.map(bankCardMapper::toDtoSummary);
    }

    /**
     * Подсчёт сущностей, соответствующих критериям.
     */
    public long countByCriteria(BankCardCriteria criteria) {
        log.debug("Count by criteria: {}", criteria);
        
        Specification<BankCard> specification = createSpecification(criteria);
        return bankCardRepository.count(specification);
    }

    /**
     * Конвертация BankCardCriteria в JPA Specification.
     */
    private Specification<BankCard> createSpecification(BankCardCriteria criteria) {
        Specification<BankCard> specification = Specification.where(null);
        
        if (criteria == null) {
            return specification;
        }

        if (criteria.getId() != null) {
            specification = specification.and(buildSpecification(criteria.getId(), BankCard_.id));
        }

        if (criteria.getCardHolderName() != null) {
            specification = specification.and(buildStringSpecification(criteria.getCardHolderName(), BankCard_.cardHolderName));
        }

        if (criteria.getStatus() != null) {
            specification = specification.and(buildSpecification(criteria.getStatus(), BankCard_.status));
        }

        if (criteria.getBalance() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getBalance(), BankCard_.balance));
        }

        if (criteria.getExpirationDate() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getExpirationDate(), BankCard_.expirationDate));
        }

        if (criteria.getUserId() != null) {
            specification = specification.and(buildReferringEntitySpecification(
                    criteria.getUserId(),
                    BankCard_.user,
                    User_.id,
                    JoinType.LEFT
            ));
        }

        if (criteria.getCreatedAt() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), BankCard_.createdAt));
        }

        return specification;
    }
}
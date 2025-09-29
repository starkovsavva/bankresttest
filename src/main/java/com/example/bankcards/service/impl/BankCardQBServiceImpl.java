//package com.example.bankcards.service.impl;
//
//import com.example.bankcards.dto.BankCardDTO;
//import com.example.bankcards.entity.BankCard;
//import com.example.bankcards.repository.BankCardRepository;
//import com.example.bankcards.service.criteria.BankCardCriteria;
//import com.example.bankcards.service.criteria.QueryBuilderService;
//import com.example.bankcards.service.mapper.BankCardMapper;
//import com.example.bankcards.service.mapper.BankCardQBService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.persistence.criteria.JoinType;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional(readOnly = true)
//public class BankCardQBServiceImpl extends QueryBuilderService<BankCard> implements BankCardQBService {
//
//    private final Logger log = LoggerFactory.getLogger(BankCardQBServiceImpl.class);
//
//    private final BankCardRepository bankCardRepository;
//    private final BankCardMapper bankCardMapper;
//
//    public BankCardQBServiceImpl(BankCardRepository bankCardRepository, BankCardMapper bankCardMapper) {
//        this.bankCardRepository = bankCardRepository;
//        this.bankCardMapper = bankCardMapper;
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Page<BankCardDTO> findByCriteria(BankCardCriteria criteria, Pageable pageable) {
//        log.debug("Request to find BankCards by criteria: {}", criteria);
//
//        Specification<BankCard> specification = buildSpecification(criteria);
//        Page<BankCard> result = getPaginatedBankCards(specification, pageable);
//
//        return result.map(bankCardMapper::toDto);
//    }
//
//    /**
//     * Function to convert {@link BankCardCriteria} to a {@link Specification}
//     *
//     * @param criteria The object which holds all the filters, which the entities should match.
//     * @return the matching {@link Specification} of the entity.
//     */
//    private Specification<BankCard> buildSpecification(BankCardCriteria criteria) {
//        Specification<BankCard> specification = Specification.where(null);
//
//        if (criteria != null) {
//            // ID filter
//            if (criteria.getId() != null) {
//                specification = specification.and(buildFilterSpecification(criteria.getId(), BankCard_.id));
//                log.debug("Added ID filter specification");
//            }
//
//            // Card holder name filter
//            if (criteria.getCardHolderName() != null) {
//                specification = specification.and(buildFilterSpecification(criteria.getCardHolderName(), BankCard_.cardHolderName));
//                log.debug("Added card holder name filter specification");
//            }
//
//            // Status filter
//            if (criteria.getStatus() != null) {
//                specification = specification.and(buildFilterSpecification(criteria.getStatus(), BankCard_.status));
//                log.debug("Added status filter specification");
//            }
//
//            // Balance filter
//            if (criteria.getBalance() != null) {
//                specification = specification.and(buildFilterSpecification(criteria.getBalance(), BankCard_.balance));
//                log.debug("Added balance filter specification");
//            }
//
//            // Expiration date filter
//            if (criteria.getExpirationDate() != null) {
//                specification = specification.and(buildFilterSpecification(criteria.getExpirationDate(), BankCard_.expirationDate));
//                log.debug("Added expiration date filter specification");
//            }
//
//            // User ID filter (nested relationship - like ingredients in Recipe)
//            if (criteria.getUserId() != null) {
//                specification = specification.and(
//                        buildSpecification(
//                                criteria.getUserId(),
//                                root -> root.join(BankCard_.user, JoinType.LEFT).get(User_.id)
//                        )
//                );
//                log.debug("Added user ID filter specification");
//            }
//
//            // Created at filter
//            if (criteria.getCreatedAt() != null) {
//                specification = specification.and(buildFilterSpecification(criteria.getCreatedAt(), BankCard_.createdAt));
//                log.debug("Added created at filter specification");
//            }
//        }
//
//        return specification;
//    }
//
//    /**
//     * Get paginated bank cards with proper entity loading.
//     * Similar to getPaginatedRecipes but for BankCards.
//     */
//    private Page<BankCard> getPaginatedBankCards(Specification<BankCard> specification, Pageable pageable) {
//        // First, get the paginated results without relations
//        Page<BankCard> bankCardPage = bankCardRepository.findAll(specification, pageable);
//
//        // Extract IDs for efficient relation loading
//        List<Long> bankCardIds = bankCardPage.getContent().stream()
//                .map(BankCard::getId)
//                .collect(Collectors.toList());
//
//        // Load bank cards with relations (user) in single query
//        List<BankCard> bankCardsWithRelations = bankCardRepository.findAllWithUserByIds(bankCardIds);
//
//        // Return new page with loaded relations
//        return new PageImpl<>(bankCardsWithRelations, pageable, bankCardPage.getTotalElements());
//    }
//}
package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.dto.requests.CardCreateRequest;
import com.example.bankcards.dto.requests.CardUpdateRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * REST controller for managing {@link BankCard}.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankCardResource {

    private final Logger log = LoggerFactory.getLogger(BankCardResource.class);
    private static final String ENTITY_NAME = "BankCard";
    private final BankCardService bankCardService;

    /**
     * {@code POST  /cards} : Create a new bank card.
     *
     * @param cardCreateRequest the BankCardDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cardCreateRequest,
     *
     */
    @PostMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankCardDTO> createCard(@Valid @RequestBody CardCreateRequest cardCreateRequest) throws URISyntaxException {
        log.debug("REST request to save Card : {}", cardCreateRequest);

        if (cardCreateRequest.cardNumber() == null) {
            throw new BadRequestException("Card number must be present", ENTITY_NAME, "cardnumbernull");
        }

        BankCardDTO result = bankCardService.save(cardCreateRequest);
        return ResponseEntity
                .created(new URI("/api/cards/" + result.id()))
                .body(result);
    }

    /**
     * {@code PUT  /cards/:id} : Updates an existing card.
     *
     * @param id the id of the BankCardDTO to save.
     * @param BankCardDTO the BankCardDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated BankCardDTO,
     * or with status {@code 400 (Bad Request)} if the BankCardDTO is not valid.
     */
    @PutMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankCardDTO> updateCard(@PathVariable Long id, @Valid @RequestBody BankCardDTO BankCardDTO) throws URISyntaxException {
        log.debug("REST request to update Card : {}, {}", id, BankCardDTO);

        if (BankCardDTO.id() == null) {
            throw new BadRequestException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!id.equals(BankCardDTO.id())) {
            throw new BadRequestException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        BankCardDTO result = bankCardService.update(BankCardDTO);
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code GET  /cards} : get all the cards.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cards in body.
     */
    @GetMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BankCardDTO>> getAllCards(@ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Cards");
        Page<BankCardDTO> page = bankCardService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code DELETE  /cards/:id} : delete the "id" card.
     *
     * @param id the id of the BankCardDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        log.debug("REST request to delete Card : {}", id);
        bankCardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET  /cards/:id} : get the "id" card.
     *
     * @param id the id of the BankCardDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the BankCardDTO,
     * or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/cards/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankCardDTO> getCard(@PathVariable Long id) {
        log.debug("REST request to get Card : {}", id);
        BankCardDTO BankCardDTO = bankCardService.findOne(id);
        return ResponseEntity.ok(BankCardDTO);
    }



    /**
     * {@code GET  /my-cards} : get all cards for current user.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cards in body.
     */
    @GetMapping("/my-cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BankCardDTO>> getMyCards(@ParameterObject Pageable pageable) {
        log.debug("REST request to get current user's Cards");
        Long userId = getCurrentUserId();
        Page<BankCardDTO> page = bankCardService.findByUserId(userId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code POST  /cards/transfer} : transfer between user's own cards.
     *
     * @param transferRequest the transfer request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PostMapping("/cards/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transferBetweenCards(@Valid @RequestBody TransferRequest transferRequest) {
        log.debug("REST request to transfer between cards: {}", transferRequest);
        Long userId = getCurrentUserId();
        bankCardService.transferBetweenOwnCards(transferRequest, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * {@code PUT  /cards/:id/block} : block a card.
     *
     * @param id the id of the card to block.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated BankCardDTO.
     */
    @PutMapping("/cards/{id}/block")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankCardDTO> blockCard(@PathVariable Long id) {
        log.debug("REST request to block Card : {}", id);
        Long userId = getCurrentUserId();
        BankCardDTO result = bankCardService.blockCard(id, userId);
        return ResponseEntity.ok(result);
    }

    private Long getCurrentUserId() {
        // Реализация получения ID текущего пользователя
        return 1L; // Заглушка
    }
}
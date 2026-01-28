package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.dto.requests.CardCreateRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Bank Cards", description = "Bank card management endpoints")
public class BankCardController {

    private static final String ENTITY_NAME = "BankCard";
    private final BankCardService bankCardService;

    @Operation(summary = "Create a new bank card")
    @PostMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankCardDTO> createCard(@Valid @RequestBody CardCreateRequest cardCreateRequest) throws URISyntaxException {
        log.debug("REST request to save Card: {}", cardCreateRequest);

        if (cardCreateRequest.cardNumber() == null) {
            throw new BadRequestException("Card number must be present", ENTITY_NAME, "cardnumbernull");
        }

        BankCardDTO result = bankCardService.save(cardCreateRequest);
        return ResponseEntity
                .created(new URI("/api/cards/" + result.id()))
                .body(result);
    }

    @Operation(summary = "Update an existing card")
    @PutMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankCardDTO> updateCard(@PathVariable Long id, @Valid @RequestBody BankCardDTO bankCardDTO) {
        log.debug("REST request to update Card: {}, {}", id, bankCardDTO);

        if (bankCardDTO.id() == null) {
            throw new BadRequestException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!id.equals(bankCardDTO.id())) {
            throw new BadRequestException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        BankCardDTO result = bankCardService.update(bankCardDTO);
        return ResponseEntity.ok().body(result);
    }

    @Operation(summary = "Get all cards with pagination")
    @GetMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BankCardDTO>> getAllCards(@ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Cards");
        Page<BankCardDTO> page = bankCardService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @Operation(summary = "Delete a card by ID")
    @DeleteMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        log.debug("REST request to delete Card: {}", id);
        bankCardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a card by ID")
    @GetMapping("/cards/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankCardDTO> getCard(@PathVariable Long id) {
        log.debug("REST request to get Card: {}", id);
        BankCardDTO bankCardDTO = bankCardService.findOne(id);
        return ResponseEntity.ok(bankCardDTO);
    }

    @Operation(summary = "Get current user's cards")
    @GetMapping("/my-cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BankCardDTO>> getMyCards(@ParameterObject Pageable pageable) {
        log.debug("REST request to get current user's Cards");
        Long userId = getCurrentUserId();
        Page<BankCardDTO> page = bankCardService.findByUserId(userId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @Operation(summary = "Transfer between user's own cards")
    @PostMapping("/cards/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transferBetweenCards(@Valid @RequestBody TransferRequest transferRequest) {
        log.debug("REST request to transfer between cards: {}", transferRequest);
        Long userId = getCurrentUserId();
        bankCardService.transferBetweenOwnCards(transferRequest, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Block a card")
    @PutMapping("/cards/{id}/block")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankCardDTO> blockCard(@PathVariable Long id) {
        log.debug("REST request to block Card: {}", id);
        Long userId = getCurrentUserId();
        BankCardDTO result = bankCardService.blockCard(id, userId);
        return ResponseEntity.ok(result);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }
}
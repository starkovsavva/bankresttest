package com.example.bankcards.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic wrapper for paginated responses containing both content and pagination metadata.
 *
 * @param <T> the type of elements in the page
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    /**
     * Creates a PageResponse from a Spring Data Page object.
     *
     * @param page the Spring Data Page
     * @param <T>  the element type
     * @return PageResponse with pagination metadata
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}

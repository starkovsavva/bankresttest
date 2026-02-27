package com.example.bankcards.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Обёртка для постраничных ответов, содержащая данные и метаинформацию о пагинации.
 *
 * @param <T> тип элементов на странице
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
     * Создаёт PageResponse из объекта Spring Data Page.
     *
     * @param page объект Spring Data Page
     * @param <T>  тип элемента
     * @return PageResponse с метаинформацией о пагинации
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

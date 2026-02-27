package com.example.bankcards.service.criteria;

import java.io.Serializable;

/**
 * Базовый интерфейс для всех классов критериев.
 * Предоставляет общий контракт для запросов на основе фильтров.
 */
public interface Criteria extends Serializable {
    
    /**
     * Создаёт копию данного объекта критериев.
     * @return новая копия критериев
     */
    Criteria copy();
    
    /**
     * Проверяет, не установлены ли фильтры в критериях.
     * @return true, если фильтры не установлены
     */
    boolean isEmpty();
}

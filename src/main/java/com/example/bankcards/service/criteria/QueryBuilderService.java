package com.example.bankcards.service.criteria;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.function.Function;

/**
 * Абстрактный базовый класс для построения JPA Specification из критериев Filter.
 * Предоставляет переиспользуемые методы для построения типобезопасных запросов.
 *
 * @param <E> тип сущности
 */
public abstract class QueryBuilderService<E> {

    /**
     * Построение спецификации для одиночного атрибута с использованием фильтра.
     */
    protected <X> Specification<E> buildSpecification(Filter<X> filter, SingularAttribute<? super E, X> field) {
        return buildSpecification(filter, root -> root.get(field));
    }

    /**
     * Построение спецификации для строкового атрибута с использованием фильтра.
     * Поддерживает операции contains/notContains для строковых типов.
     */
    protected Specification<E> buildStringSpecification(Filter<String> filter, SingularAttribute<? super E, String> field) {
        return buildStringSpecification(filter, root -> root.get(field));
    }

    /**
     * Построение спецификации для атрибута типа Comparable с использованием диапазонного фильтра.
     * Поддерживает операции greaterThan, lessThan, greaterOrEqual, lessOrEqual.
     */
    protected <X extends Comparable<? super X>> Specification<E> buildRangeSpecification(
            Filter<X> filter, SingularAttribute<? super E, X> field) {
        return buildRangeSpecification(filter, root -> root.get(field));
    }

    /**
     * Построение спецификации с использованием пользовательской функции пути.
     */
    @SuppressWarnings("unchecked")
    protected <X> Specification<E> buildSpecification(Filter<X> filter, Function<Root<E>, Expression<X>> pathFunction) {
        if (filter == null) {
            return null;
        }

        Specification<E> result = Specification.where(null);

        if (filter.getEquals() != null) {
            result = result.and(equalsSpecification(pathFunction, filter.getEquals()));
        }
        if (filter.getNotEquals() != null) {
            result = result.and(notEqualsSpecification(pathFunction, filter.getNotEquals()));
        }
        if (filter.getIn() != null && !filter.getIn().isEmpty()) {
            result = result.and(inSpecification(pathFunction, filter.getIn()));
        }
        if (filter.getNotIn() != null && !filter.getNotIn().isEmpty()) {
            result = result.and(notInSpecification(pathFunction, filter.getNotIn()));
        }

        return result;
    }

    /**
     * Построение строковой спецификации с поддержкой LIKE.
     */
    protected Specification<E> buildStringSpecification(Filter<String> filter, Function<Root<E>, Expression<String>> pathFunction) {
        if (filter == null) {
            return null;
        }

        Specification<E> result = buildSpecification(filter, pathFunction);

        if (filter.getContains() != null) {
            result = result.and(containsSpecification(pathFunction, filter.getContains()));
        }
        if (filter.getNotContains() != null) {
            result = result.and(notContainsSpecification(pathFunction, filter.getNotContains()));
        }

        return result;
    }

    /**
     * Построение диапазонной спецификации для типов Comparable.
     */
    protected <X extends Comparable<? super X>> Specification<E> buildRangeSpecification(
            Filter<X> filter, Function<Root<E>, Expression<X>> pathFunction) {
        if (filter == null) {
            return null;
        }

        Specification<E> result = buildSpecification(filter, pathFunction);

        if (filter.getGreaterThan() != null) {
            result = result.and(greaterThanSpecification(pathFunction, filter.getGreaterThan()));
        }
        if (filter.getLessThan() != null) {
            result = result.and(lessThanSpecification(pathFunction, filter.getLessThan()));
        }
        if (filter.getGreaterOrEqual() != null) {
            result = result.and(greaterOrEqualSpecification(pathFunction, filter.getGreaterOrEqual()));
        }
        if (filter.getLessOrEqual() != null) {
            result = result.and(lessOrEqualSpecification(pathFunction, filter.getLessOrEqual()));
        }

        return result;
    }

    /**
     * Построение спецификации для связанной сущности по её ID.
     */
    protected <X, Y> Specification<E> buildReferringEntitySpecification(
            Filter<X> filter,
            SingularAttribute<? super E, Y> reference,
            SingularAttribute<? super Y, X> valueField) {
        return buildSpecification(filter, root -> root.get(reference).get(valueField));
    }

    /**
     * Построение спецификации для связанной сущности с использованием join.
     */
    protected <Y> Specification<E> buildReferringEntitySpecification(
            Filter<Long> filter,
            SingularAttribute<? super E, Y> reference,
            SingularAttribute<? super Y, Long> idField,
            JoinType joinType) {
        return (root, query, builder) -> {
            if (filter == null || filter.isEmpty()) {
                return null;
            }
            Join<E, Y> join = root.join(reference, joinType);
            return buildSpecification(filter, r -> join.get(idField)).toPredicate(root, query, builder);
        };
    }

    // === Приватные вспомогательные методы для построения спецификаций ===

    private <X> Specification<E> equalsSpecification(Function<Root<E>, Expression<X>> pathFunction, X value) {
        return (root, query, builder) -> builder.equal(pathFunction.apply(root), value);
    }

    private <X> Specification<E> notEqualsSpecification(Function<Root<E>, Expression<X>> pathFunction, X value) {
        return (root, query, builder) -> builder.notEqual(pathFunction.apply(root), value);
    }

    private <X> Specification<E> inSpecification(Function<Root<E>, Expression<X>> pathFunction, Collection<X> values) {
        return (root, query, builder) -> {
            CriteriaBuilder.In<X> in = builder.in(pathFunction.apply(root));
            for (X value : values) {
                in = in.value(value);
            }
            return in;
        };
    }

    private <X> Specification<E> notInSpecification(Function<Root<E>, Expression<X>> pathFunction, Collection<X> values) {
        return (root, query, builder) -> {
            CriteriaBuilder.In<X> in = builder.in(pathFunction.apply(root));
            for (X value : values) {
                in = in.value(value);
            }
            return builder.not(in);
        };
    }

    private Specification<E> containsSpecification(Function<Root<E>, Expression<String>> pathFunction, String value) {
        return (root, query, builder) ->
                builder.like(builder.upper(pathFunction.apply(root)), wrapLikeQuery(value));
    }

    private Specification<E> notContainsSpecification(Function<Root<E>, Expression<String>> pathFunction, String value) {
        return (root, query, builder) ->
                builder.notLike(builder.upper(pathFunction.apply(root)), wrapLikeQuery(value));
    }

    private <X extends Comparable<? super X>> Specification<E> greaterThanSpecification(
            Function<Root<E>, Expression<X>> pathFunction, X value) {
        return (root, query, builder) -> builder.greaterThan(pathFunction.apply(root), value);
    }

    private <X extends Comparable<? super X>> Specification<E> lessThanSpecification(
            Function<Root<E>, Expression<X>> pathFunction, X value) {
        return (root, query, builder) -> builder.lessThan(pathFunction.apply(root), value);
    }

    private <X extends Comparable<? super X>> Specification<E> greaterOrEqualSpecification(
            Function<Root<E>, Expression<X>> pathFunction, X value) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(pathFunction.apply(root), value);
    }

    private <X extends Comparable<? super X>> Specification<E> lessOrEqualSpecification(
            Function<Root<E>, Expression<X>> pathFunction, X value) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(pathFunction.apply(root), value);
    }

    /**
     * Оборачивает строку для LIKE-запроса с подстановочными символами.
     */
    protected String wrapLikeQuery(String text) {
        return "%" + text.toUpperCase() + "%";
    }

    /**
     * Создание distinct-запроса для избежания дубликатов при использовании join.
     */
    protected <X> Specification<E> distinct(boolean distinct) {
        return (root, query, builder) -> {
            query.distinct(distinct);
            return null;
        };
    }
}

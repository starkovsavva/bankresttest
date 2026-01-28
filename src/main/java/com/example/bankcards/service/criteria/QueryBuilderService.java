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
 * Abstract base class for building JPA Specifications from Filter criteria.
 * Provides reusable methods for constructing type-safe queries.
 *
 * @param <E> the entity type
 */
public abstract class QueryBuilderService<E> {

    /**
     * Build a specification for a singular attribute using a filter.
     */
    protected <X> Specification<E> buildSpecification(Filter<X> filter, SingularAttribute<? super E, X> field) {
        return buildSpecification(filter, root -> root.get(field));
    }

    /**
     * Build a specification for a String attribute using a filter.
     * Supports contains/notContains operations for String types.
     */
    protected Specification<E> buildStringSpecification(Filter<String> filter, SingularAttribute<? super E, String> field) {
        return buildStringSpecification(filter, root -> root.get(field));
    }

    /**
     * Build a specification for a Comparable attribute using a range filter.
     * Supports greaterThan, lessThan, greaterOrEqual, lessOrEqual operations.
     */
    protected <X extends Comparable<? super X>> Specification<E> buildRangeSpecification(
            Filter<X> filter, SingularAttribute<? super E, X> field) {
        return buildRangeSpecification(filter, root -> root.get(field));
    }

    /**
     * Build a specification using a custom path function.
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
     * Build a string specification with LIKE support.
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
     * Build a range specification for comparable types.
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
     * Build a specification for a reference entity using its ID.
     */
    protected <X, Y> Specification<E> buildReferringEntitySpecification(
            Filter<X> filter,
            SingularAttribute<? super E, Y> reference,
            SingularAttribute<? super Y, X> valueField) {
        return buildSpecification(filter, root -> root.get(reference).get(valueField));
    }

    /**
     * Build a specification for a reference entity using join.
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

    // === Private helper methods for building specifications ===

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
     * Wrap a string for LIKE query with wildcards.
     */
    protected String wrapLikeQuery(String text) {
        return "%" + text.toUpperCase() + "%";
    }

    /**
     * Create a distinct query to avoid duplicates when using joins.
     */
    protected <X> Specification<E> distinct(boolean distinct) {
        return (root, query, builder) -> {
            query.distinct(distinct);
            return null;
        };
    }
}

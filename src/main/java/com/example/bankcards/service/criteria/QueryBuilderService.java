package com.example.bankcards.service.criteria;


import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.function.Function;

public abstract class QueryBuilderService<E> {
    protected <X> Specification<E> buildFilterSpecification(Filter<X> filter, SingularAttribute<? super E, X>
            field) {
        if (String.class.isAssignableFrom(field.getJavaType())) {
            return buildSpecification((Filter<String>) filter, root -> root.get((SingularAttribute<? super E, String>) field));
        } else {
            return buildSpecificationGeneric(filter, root -> root.get(field));
        }
    }

    protected <X> Specification<E> buildSpecificationGeneric(Filter<X> filter, Function<Root<E>, Expression<X>> metaclassFn) {
        if (filter.getEquals() != null) {
            return (root, query, builder) -> builder.equal(metaclassFn.apply(root), filter.getEq());
        } else if (filter.getNotEquals() != null) {
            return (root, query, builder) -> builder.not(builder.equal(metaclassFn.apply(root), filter.getNeq()));
        } else if (filter.getIn() != null) {
            return (root, query, builder) -> {
                CriteriaBuilder.In<X> in = builder.in(metaclassFn.apply(root));
                for (X value : filter.getIn()) {
                    in = in.value(value);
                }
                return in;
            };
        } else if (filter.getNotIn() != null) {
            return (root, query, builder) -> {
                CriteriaBuilder.In<X> in = builder.in(metaclassFn.apply(root));
                for (X value : filter.getNotIn()) {
                    in = in.value(value);
                }
                return builder.not(in);
            };
        }
        return null;
    }

    protected Specification<E> buildSpecification(Filter<String> filter, Function<Root<E>, Expression<String>> metaclassFn) {
        if (filter.getContains() != null) {
            return (root, query, builder) -> builder.like(builder.upper(metaclassFn.apply(root)), wrapLikeQuery(filter.getContains()));
        } else if (filter.getNotContains() != null) {
            return (root, query, builder) -> builder.not(builder.like(builder.upper(metaclassFn.apply(root)), wrapLikeQuery(filter.getNotContains())));
        } else {
            return buildSpecificationGeneric(filter, metaclassFn);
        }
    }

    protected String wrapLikeQuery(String txt) {
        return "%" + txt.toUpperCase() + '%';
    }
}

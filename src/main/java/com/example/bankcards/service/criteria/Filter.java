package com.example.bankcards.service.criteria;

import java.util.ArrayList;
import java.util.List;

public class Filter<FIELD_TYPE> {

    private FIELD_TYPE equals;
    private FIELD_TYPE notEquals;
    private List<FIELD_TYPE> in;
    private List<FIELD_TYPE> notIn;
    private FIELD_TYPE contains;
    private FIELD_TYPE notContains;
    private FIELD_TYPE greaterThan;
    private FIELD_TYPE lessThan;
    private FIELD_TYPE greaterOrEqual;
    private FIELD_TYPE lessOrEqual;

    public Filter() {
    }

    public Filter(Filter<FIELD_TYPE> filter) {
        this.equals = filter.equals;
        this.notEquals = filter.notEquals;
        this.in = filter.in == null ? null : new ArrayList<>(filter.in);
        this.notIn = filter.notIn == null ? null : new ArrayList<>(filter.notIn);
        this.contains = filter.contains;
        this.notContains = filter.notContains;
        this.greaterThan = filter.greaterThan;
        this.lessThan = filter.lessThan;
        this.greaterOrEqual = filter.greaterOrEqual;
        this.lessOrEqual = filter.lessOrEqual;
    }

    public Filter<FIELD_TYPE> copy() {
        return new Filter<>(this);
    }

    public static <T> Filter<T> eq(T value) {
        Filter<T> filter = new Filter<>();
        filter.setEquals(value);
        return filter;
    }

    public static <T> Filter<T> in(List<T> values) {
        Filter<T> filter = new Filter<>();
        filter.setIn(values);
        return filter;
    }

    public static Filter<String> contains(String value) {
        Filter<String> filter = new Filter<>();
        filter.setContains(value);
        return filter;
    }


    public FIELD_TYPE getEquals() { return equals; }
    public void setEquals(FIELD_TYPE equals) { this.equals = equals; }

    public FIELD_TYPE getNotEquals() { return notEquals; }
    public void setNotEquals(FIELD_TYPE notEquals) { this.notEquals = notEquals; }

    public List<FIELD_TYPE> getIn() { return in; }
    public void setIn(List<FIELD_TYPE> in) { this.in = in; }

    public List<FIELD_TYPE> getNotIn() { return notIn; }
    public void setNotIn(List<FIELD_TYPE> notIn) { this.notIn = notIn; }

    public FIELD_TYPE getContains() { return contains; }
    public void setContains(FIELD_TYPE contains) { this.contains = contains; }

    public FIELD_TYPE getNotContains() { return notContains; }
    public void setNotContains(FIELD_TYPE notContains) { this.notContains = notContains; }

    public FIELD_TYPE getGreaterThan() { return greaterThan; }
    public void setGreaterThan(FIELD_TYPE greaterThan) { this.greaterThan = greaterThan; }

    public FIELD_TYPE getLessThan() { return lessThan; }
    public void setLessThan(FIELD_TYPE lessThan) { this.lessThan = lessThan; }

    public FIELD_TYPE getGreaterOrEqual() { return greaterOrEqual; }
    public void setGreaterOrEqual(FIELD_TYPE greaterOrEqual) { this.greaterOrEqual = greaterOrEqual; }

    public FIELD_TYPE getLessOrEqual() { return lessOrEqual; }
    public void setLessOrEqual(FIELD_TYPE lessOrEqual) { this.lessOrEqual = lessOrEqual; }

    // === Utility methods ===
    public boolean isEmpty() {
        return equals == null && notEquals == null && in == null &&
                notIn == null && contains == null && notContains == null &&
                greaterThan == null && lessThan == null &&
                greaterOrEqual == null && lessOrEqual == null;
    }

    public boolean hasEquals() { return equals != null; }
    public boolean hasIn() { return in != null && !in.isEmpty(); }
    public boolean hasContains() { return contains != null; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Filter{");
        if (equals != null) sb.append("equals=").append(equals).append(", ");
        if (notEquals != null) sb.append("notEquals=").append(notEquals).append(", ");
        if (in != null) sb.append("in=").append(in).append(", ");
        if (notIn != null) sb.append("notIn=").append(notIn).append(", ");
        if (contains != null) sb.append("contains=").append(contains).append(", ");
        if (notContains != null) sb.append("notContains=").append(notContains).append(", ");
        if (greaterThan != null) sb.append("greaterThan=").append(greaterThan).append(", ");
        if (lessThan != null) sb.append("lessThan=").append(lessThan).append(", ");
        if (greaterOrEqual != null) sb.append("greaterOrEqual=").append(greaterOrEqual).append(", ");
        if (lessOrEqual != null) sb.append("lessOrEqual=").append(lessOrEqual);
        sb.append('}');
        return sb.toString();
    }
}

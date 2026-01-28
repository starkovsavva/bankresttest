package com.example.bankcards.service.criteria;

import java.io.Serializable;

/**
 * Base interface for all criteria classes.
 * Provides a common contract for filter-based queries.
 */
public interface Criteria extends Serializable {
    
    /**
     * Creates a copy of this criteria object.
     * @return a new copy of the criteria
     */
    Criteria copy();
    
    /**
     * Checks if the criteria has no filters set.
     * @return true if no filters are set
     */
    boolean isEmpty();
}

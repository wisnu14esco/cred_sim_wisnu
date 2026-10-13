package com.wisnu.creditsimulator.repository;

import com.wisnu.creditsimulator.model.LoanApplication;
import java.util.List;
import java.util.Optional;

/**
 * Named storage for loan applications ("sheets"). Only the inputs are stored;
 * results are recalculated when a sheet is opened.
 */
public interface SheetRepository {

    /** Saves the application under name, replacing an existing sheet with the same name. */
    void save(String name, LoanApplication application);

    Optional<LoanApplication> find(String name);

    /** Sheet names in the order they were first saved. */
    List<String> names();

    /** @return true if a sheet was deleted */
    boolean delete(String name);
}

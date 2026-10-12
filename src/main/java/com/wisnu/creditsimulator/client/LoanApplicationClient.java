package com.wisnu.creditsimulator.client;

import com.wisnu.creditsimulator.model.LoanApplication;

/**
 * Source of an existing loan calculation.
 */
public interface LoanApplicationClient {

    /**
     * @return the loan application provided by the source
     * @throws LoanServiceException 
     */
    LoanApplication fetch();
}

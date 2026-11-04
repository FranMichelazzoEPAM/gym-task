package com.example.trainer_workload_service.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionLoggingFilterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void reusesIncomingTransactionIdAndClearsItAfterRequest() throws Exception {
        String transactionId = "crm-tx-789";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainers/trainer1/workload");
        request.addHeader(TransactionLoggingFilter.TRANSACTION_ID_HEADER, transactionId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
                assertEquals(transactionId, MDC.get(TransactionLoggingFilter.MDC_KEY));

        new TransactionLoggingFilter().doFilter(request, response, chain);

        assertEquals(transactionId, response.getHeader(TransactionLoggingFilter.TRANSACTION_ID_HEADER));
        assertNull(MDC.get(TransactionLoggingFilter.MDC_KEY));
    }
}

package gym.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionIdFilterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void preservesTransactionIdHeaderAndExposesItInMdcDuringRequest() throws Exception {
        String transactionId = "external-tx-123";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainers");
        request.addHeader(TransactionIdFilter.TX_ID_HEADER, transactionId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
                assertEquals(transactionId, MDC.get(TransactionIdFilter.MDC_KEY));

        new TransactionIdFilter().doFilter(request, response, chain);

        assertEquals(transactionId, response.getHeader(TransactionIdFilter.TX_ID_HEADER));
        assertNull(MDC.get(TransactionIdFilter.MDC_KEY));
    }

    @Test
    void generatesTransactionIdWhenIncomingHeaderIsUnsafe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainers");
        request.addHeader(TransactionIdFilter.TX_ID_HEADER, "bad\nvalue");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new TransactionIdFilter().doFilter(request, response, (req, res) -> {
            String id = MDC.get(TransactionIdFilter.MDC_KEY);
            assertNotNull(id);
            assertEquals(id, response.getHeader(TransactionIdFilter.TX_ID_HEADER));
        });
    }
}

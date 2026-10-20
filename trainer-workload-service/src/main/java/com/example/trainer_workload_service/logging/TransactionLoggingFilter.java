package com.example.trainer_workload_service.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class TransactionLoggingFilter extends OncePerRequestFilter implements Ordered {
    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);
    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String MDC_KEY = "transactionId";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // run before Spring Security's filter chain
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - start;

            String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

            log.info("TRANSACTION | method={} endpoint={} requestBody={} status={} responseBody={} durationMs={}",
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    requestBody.isBlank() ? "-" : requestBody,
                    wrappedResponse.getStatus(),
                    responseBody.isBlank() ? "-" : responseBody,
                    duration);

            wrappedResponse.copyBodyToResponse();
            MDC.remove(MDC_KEY);
        }
    }
}

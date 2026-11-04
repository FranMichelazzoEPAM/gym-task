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
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class TransactionLoggingFilter extends OncePerRequestFilter implements Ordered {
    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);
    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String MDC_KEY = "transactionId";
    private static final Pattern SAFE_TRANSACTION_ID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // run before Spring Security's filter chain
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String transactionId = safeTransactionId(request.getHeader(TRANSACTION_ID_HEADER));
        String previousTransactionId = MDC.get(MDC_KEY);
        MDC.put(MDC_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        long start = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            log.info("TRANSACTION | transactionId={} method={} route={} status={} durationMs={}",
                    MDC.get(MDC_KEY), request.getMethod(), route == null ? "unmatched" : route,
                    response.getStatus(), durationMs);
            restoreMdc(previousTransactionId);
        }
    }

    private static String safeTransactionId(String transactionId) {
        return transactionId != null && SAFE_TRANSACTION_ID.matcher(transactionId).matches()
                ? transactionId
                : UUID.randomUUID().toString();
    }

    private static void restoreMdc(String transactionId) {
        if (transactionId == null) {
            MDC.remove(MDC_KEY);
        } else {
            MDC.put(MDC_KEY, transactionId);
        }
    }
}

package gym.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(1)
public class TransactionIdFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionIdFilter.class);
    private static final Pattern SAFE_TRANSACTION_ID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    public static final String TX_ID_HEADER = "X-Transaction-Id";
    public static final String MDC_KEY = "transactionId";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String transactionId = safeTransactionId(request.getHeader(TX_ID_HEADER));
        String previousTransactionId = MDC.get(MDC_KEY);

        MDC.put(MDC_KEY, transactionId);
        response.setHeader(TX_ID_HEADER, transactionId);
        long start = System.nanoTime();

        try {
            chain.doFilter(req, res);
        } finally {
            Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            LOG.info("TRANSACTION | transactionId={} method={} route={} status={} durationMs={}",
                    MDC.get(MDC_KEY), request.getMethod(), route == null ? "unmatched" : route,
                    response.getStatus(),
                    (System.nanoTime() - start) / 1_000_000);
            if (previousTransactionId == null) {
                MDC.remove(MDC_KEY);
            } else {
                MDC.put(MDC_KEY, previousTransactionId);
            }
        }
    }

    private static String safeTransactionId(String transactionId) {
        return transactionId != null && SAFE_TRANSACTION_ID.matcher(transactionId).matches()
                ? transactionId
                : UUID.randomUUID().toString();
    }
}
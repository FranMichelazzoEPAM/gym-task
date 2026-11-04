package gym.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RestCallLoggingAspect {

    private static final Logger LOG = LoggerFactory.getLogger(RestCallLoggingAspect.class);

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logCall(ProceedingJoinPoint pjp) throws Throwable {
        String signature = pjp.getSignature().toShortString();
        long start = System.nanoTime();
        LOG.info("OPERATION START | transactionId={} method={}", MDC.get("transactionId"), signature);

        try {
            Object result = pjp.proceed();
            Object status = result instanceof ResponseEntity<?> responseEntity
                    ? responseEntity.getStatusCode()
                    : "completed";
            LOG.info("OPERATION END | transactionId={} method={} status={} durationMs={}",
                    MDC.get("transactionId"), signature, status, (System.nanoTime() - start) / 1_000_000);
            return result;
        } catch (Exception ex) {
            LOG.error("OPERATION ERROR | transactionId={} method={} exceptionType={} durationMs={}",
                    MDC.get("transactionId"), signature, ex.getClass().getSimpleName(),
                    (System.nanoTime() - start) / 1_000_000);
            throw ex;
        }
    }
}
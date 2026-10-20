package gym.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RestCallLoggingAspect {

    private static final Logger LOG = LoggerFactory.getLogger(RestCallLoggingAspect.class);

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logCall(ProceedingJoinPoint pjp) throws Throwable {
        String signature = pjp.getSignature().toShortString();
        LOG.info("REST call invoked: {} | args={}", signature, Arrays.toString(pjp.getArgs()));

        try {
            Object result = pjp.proceed();
            if (result instanceof ResponseEntity<?> responseEntity) {
                LOG.info("REST call completed: {} | status={}, body={}",
                        signature, responseEntity.getStatusCode(), responseEntity.getBody());
            } else {
                LOG.info("REST call completed: {} | response={}", signature, result);
            }
            return result;
        } catch (Exception ex) {
            LOG.error("REST call failed: {} | error={}", signature, ex.getMessage());
            throw ex;
        }
    }
}
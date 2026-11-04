package com.example.trainer_workload_service.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(OperationLoggingAspect.class);

    @Around("execution(* com.example.trainer_workload_service.service..*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        long start = System.nanoTime();
        log.info("OPERATION START | transactionId={} method={}", MDC.get("transactionId"), signature);

        try {
            Object result = joinPoint.proceed();
            log.info("OPERATION END | transactionId={} method={} durationMs={}",
                    MDC.get("transactionId"), signature, (System.nanoTime() - start) / 1_000_000);
            return result;
        } catch (Exception ex) {
            log.error("OPERATION ERROR | transactionId={} method={} exceptionType={} durationMs={}",
                    MDC.get("transactionId"), signature, ex.getClass().getSimpleName(),
                    (System.nanoTime() - start) / 1_000_000);
            throw ex;
        }
    }
}

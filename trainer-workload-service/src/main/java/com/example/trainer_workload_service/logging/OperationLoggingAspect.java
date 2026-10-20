package com.example.trainer_workload_service.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class OperationLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(OperationLoggingAspect.class);

    @Around("execution(* com.example.trainer_workload_service.service..*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        log.info("OPERATION START | method={} args={}", signature, Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            log.info("OPERATION END | method={} result={}", signature, result);
            return result;
        } catch (Exception ex) {
            log.error("OPERATION ERROR | method={} message={}", signature, ex.getMessage());
            throw ex;
        }
    }
}

package com.revworkforce.discovery.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.revworkforce.discovery..*(..)) && !within(com.revworkforce.discovery.aspect..*)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        log.info("Entering {} args={}", signature, Arrays.toString(joinPoint.getArgs()));
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("Completed {} in {} ms", signature, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.error("Failed {} in {} ms: {}", signature, System.currentTimeMillis() - start, ex.getMessage());
            throw ex;
        }
    }
}

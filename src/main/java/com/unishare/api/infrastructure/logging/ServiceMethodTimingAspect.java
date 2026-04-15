package com.unishare.api.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Khi bật {@code app.logging.service-method-timing=true}, ghi DEBUG thời gian từng lời gọi
 * tới implementation trong {@code ..service.impl..} (cùng thread với request → MDC requestId vẫn có).
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "app.logging.service-method-timing", havingValue = "true")
public class ServiceMethodTimingAspect {

    @Around("execution(* com.unishare.api.modules..service.impl..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.debug("service {} took {} ms", joinPoint.getSignature().toShortString(), durationMs);
        }
    }
}

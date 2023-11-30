package com.example.circuitbraker;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CircuitBreakerAspect {

    @Before("@annotation(circuitBreaker)")
    public void beforeCustomAnnotation(JoinPoint joinPoint, CircuitBreaker circuitBreaker) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("Before CustomAnnotation - Method Name: " + methodName);
    }

    @After("@annotation(circuitBreaker)")
    public void afterCustomAnnotation(JoinPoint joinPoint, CircuitBreaker circuitBreaker) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("After CustomAnnotation - Method Name: " + methodName);
    }
}

package com.kozitskiy.rwticket.rwparserservice.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.kozitskiy.rwticket.rwparserservice.service.*.*(..))")
    public void serviceMethods(){}

    @Around("serviceMethods()")
    public Object logServiceMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable{

        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("Method call: {} with arguments: {}", methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();

        Object result;
        try {
            result = joinPoint.proceed();
        }catch (Throwable e){
            log.error("Error in {} method: {}", methodName, e.getMessage());
            throw e;
        }

        long timeTaken = System.currentTimeMillis() - startTime;
        log.info("Method: {} completed in {} ms.", methodName, timeTaken);

        return result;
    }
}

package com.telecom.campaign.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.telecom.campaign.user.entity.User;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    @Around("execution(* com.telecom.campaign..controller.*.*(..)) && " +
            "!execution(* com.telecom.campaign..controller.AuthController.login(..))")
    public Object auditControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String user = getAuthenticatedUserEmail();
        String action = className + "." + method;

        log.info("AUDIT | user={} | action={} | args={}", user, action, truncateArgs(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            log.info("AUDIT | user={} | action={} | status=SUCCESS", user, action);
            return result;
        } catch (Throwable e) {
            log.warn("AUDIT | user={} | action={} | status=FAILED | error={}", user, action, e.getMessage());
            throw e;
        }
    }

    private String getAuthenticatedUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user.getEmail();
        }
        return "anonymous";
    }

    private String truncateArgs(Object[] args) {
        if (args == null || args.length == 0) return "none";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            String argStr = args[i] != null ? args[i].toString() : "null";
            if (argStr.length() > 100) {
                sb.append(argStr, 0, 100).append("...");
            } else {
                sb.append(argStr);
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

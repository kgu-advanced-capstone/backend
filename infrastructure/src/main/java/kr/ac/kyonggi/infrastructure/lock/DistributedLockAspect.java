package kr.ac.kyonggi.infrastructure.lock;

import kr.ac.kyonggi.common.lock.DistributedLock;
import kr.ac.kyonggi.common.lock.LockProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class DistributedLockAspect {

    private static final String LOCK_PREFIX = "lock:";

    private final ObjectProvider<LockProvider> lockProviderProvider;
    private final ExpressionParser parser = new SpelExpressionParser();

    public DistributedLockAspect(ObjectProvider<LockProvider> lockProviderProvider) {
        this.lockProviderProvider = lockProviderProvider;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        LockProvider lockProvider = lockProviderProvider.getIfAvailable();
        if (lockProvider == null) {
            throw new IllegalStateException(
                    "분산락을 사용하려면 LockProvider 빈이 필요합니다: " + joinPoint.getSignature());
        }

        String lockKey = LOCK_PREFIX + resolveKey(joinPoint, distributedLock.key());
        lockProvider.lock(lockKey, distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
        try {
            return joinPoint.proceed();
        } finally {
            lockProvider.unlock(lockKey);
        }
    }

    private String resolveKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
        }
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}

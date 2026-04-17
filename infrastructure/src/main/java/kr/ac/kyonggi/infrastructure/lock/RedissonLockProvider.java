package kr.ac.kyonggi.infrastructure.lock;

import kr.ac.kyonggi.common.exception.LockAcquisitionException;
import kr.ac.kyonggi.common.lock.LockProvider;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedissonLockProvider implements LockProvider {

    private final ObjectProvider<RedissonClient> redissonClientProvider;

    public RedissonLockProvider(ObjectProvider<RedissonClient> redissonClientProvider) {
        this.redissonClientProvider = redissonClientProvider;
    }

    @Override
    public void lock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RedissonClient client = redissonClientProvider.getIfAvailable();
        if (client == null) {
            throw new IllegalStateException("분산락을 사용하려면 RedissonClient 빈이 필요합니다: " + lockKey);
        }
        RLock rLock = client.getLock(lockKey);
        try {
            if (!rLock.tryLock(waitTime, leaseTime, timeUnit)) {
                throw new LockAcquisitionException("락 획득 타임아웃: " + lockKey);
            }
            log.debug("분산락 획득: {}", lockKey);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("락 획득 중 인터럽트 발생: " + lockKey);
        }
    }

    @Override
    public void unlock(String lockKey) {
        RedissonClient client = redissonClientProvider.getIfAvailable();
        if (client == null) return;
        RLock rLock = client.getLock(lockKey);
        if (rLock.isHeldByCurrentThread()) {
            rLock.unlock();
            log.debug("분산락 해제: {}", lockKey);
        }
    }
}

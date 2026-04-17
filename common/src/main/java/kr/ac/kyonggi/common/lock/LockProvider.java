package kr.ac.kyonggi.common.lock;

import java.util.concurrent.TimeUnit;

public interface LockProvider {
    void lock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit);
    void unlock(String lockKey);
}

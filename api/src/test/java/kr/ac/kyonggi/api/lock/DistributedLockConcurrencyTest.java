package kr.ac.kyonggi.api.lock;

import kr.ac.kyonggi.common.exception.LockAcquisitionException;
import kr.ac.kyonggi.common.lock.DistributedLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Testcontainers
class DistributedLockConcurrencyTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private CounterService counterService;

    @Autowired
    private RedissonClient redissonClient;

    @BeforeEach
    void setUp() {
        counterService.reset();
        redissonClient.getLock("lock:timeout-test-key").forceUnlock();
    }

    @Test
    @DisplayName("분산락 적용 시 동시 접근에서 카운터 정합성을 보장한다")
    void distributedLock_withConcurrentAccess_guaranteesCorrectCount() throws InterruptedException {
        int threadCount = 50;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    counterService.increment();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        assertThat(ready.await(30, TimeUnit.SECONDS)).isTrue();  // 모든 스레드 준비 대기
        start.countDown();     // 동시 시작 신호
        try {
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }

        assertThat(counterService.getCount()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("락 대기 시간 초과 시 LockAcquisitionException이 발생한다")
    void distributedLock_whenLockAlreadyHeld_throwsLockAcquisitionException() throws InterruptedException {
        // given: 다른 스레드가 락을 점유 (Redisson은 스레드 단위로 소유권을 관리하므로 별도 스레드 필요)
        CountDownLatch lockHeld = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService holder = Executors.newSingleThreadExecutor();

        holder.submit(() -> {
            redissonClient.getLock("lock:timeout-test-key").lock();
            lockHeld.countDown();
            try {
                release.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                redissonClient.getLock("lock:timeout-test-key").unlock();
            }
        });

        assertThat(lockHeld.await(5, TimeUnit.SECONDS)).isTrue();

        try {
            // when & then: waitTime(1초) 이내 락 획득 실패 → 예외 발생
            assertThatThrownBy(() -> counterService.incrementWithShortWait())
                    .isInstanceOf(LockAcquisitionException.class)
                    .hasMessageContaining("락 획득 타임아웃");
        } finally {
            release.countDown();
            holder.shutdown();
            holder.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        CounterService counterService() {
            return new CounterService();
        }

        @Bean(destroyMethod = "shutdown")
        RedissonClient redissonClient(
                @Value("${spring.data.redis.host}") String host,
                @Value("${spring.data.redis.port}") int port) {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + host + ":" + port);
            return Redisson.create(config);
        }
    }

    /**
     * 분산락 테스트용 카운터 서비스.
     * increment()는 의도적으로 read-sleep-write 패턴을 사용하여
     * 락 없이는 반드시 경쟁 조건이 발생하도록 설계되었다.
     */
    static class CounterService {

        private int counter = 0;

        @DistributedLock(key = "'concurrency-counter'", waitTime = 5, leaseTime = 1)
        public void increment() throws InterruptedException {
            int current = counter;
            Thread.sleep(10);  // 경쟁 조건 노출을 위한 지연
            counter = current + 1;
        }

        @DistributedLock(key = "'timeout-test-key'", waitTime = 1, leaseTime = 5)
        public void incrementWithShortWait() {
            counter++;
        }

        public int getCount() {
            return counter;
        }

        public void reset() {
            counter = 0;
        }
    }
}

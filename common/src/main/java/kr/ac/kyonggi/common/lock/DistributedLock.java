package kr.ac.kyonggi.common.lock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 분산락 어노테이션.
 *
 * <p>key는 SpEL 표현식을 지원한다.
 * <ul>
 *   <li>리터럴: {@code key = "'my-resource'"}</li>
 *   <li>파라미터 참조: {@code key = "#userId"}</li>
 *   <li>조합: {@code key = "'project:' + #projectId"}</li>
 * </ul>
 *
 * <pre>{@code
 * @DistributedLock(key = "'project:apply:' + #projectId")
 * public void apply(Long projectId) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    String key();

    long waitTime() default 5;

    long leaseTime() default 3;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}

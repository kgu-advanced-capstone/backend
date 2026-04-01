package kr.ac.kyonggi.api.project;

import kr.ac.kyonggi.domain.project.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ProjectApplyConcurrencyTest {

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @AfterEach
    void tearDown() {
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    @DisplayName("최대 인원보다 많은 동시 요청이 들어와도 currentMemberCount가 maxMembers를 초과하지 않는다")
    void concurrentApply_neverExceedsMaxMembers() throws InterruptedException {
        int maxMembers = 5;
        int threadCount = 10;

        Project project = projectRepository.save(Project.create(new ProjectCreateCommand(
                "동시성 테스트", "설명", "백엔드", List.of("Java"), maxMembers,
                LocalDate.of(2026, 12, 31), 0L
        )));

        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (long userId = 1; userId <= threadCount; userId++) {
            final long uid = userId;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    projectService.apply(project.getId(), uid);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(maxMembers);
        assertThat(projectMemberRepository.countByProjectId(project.getId())).isEqualTo(maxMembers);
        Project reloaded = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(reloaded.getCurrentMemberCount()).isEqualTo(maxMembers);
    }

    @Test
    @DisplayName("같은 사용자의 동시 중복 요청 중 정확히 하나만 성공한다")
    void concurrentApply_sameUser_exactlyOneSucceeds() throws InterruptedException {
        int threadCount = 5;

        Project project = projectRepository.save(Project.create(new ProjectCreateCommand(
                "중복 방지 테스트", "설명", "백엔드", List.of("Java"), threadCount,
                LocalDate.of(2026, 12, 31), 0L
        )));

        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    projectService.apply(project.getId(), 1L); // 같은 사용자
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(projectMemberRepository.countByProjectId(project.getId())).isEqualTo(1);
        Project reloaded = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(reloaded.getCurrentMemberCount()).isEqualTo(1);
    }
}

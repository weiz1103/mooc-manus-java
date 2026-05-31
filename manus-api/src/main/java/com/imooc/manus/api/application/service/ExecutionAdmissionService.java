package com.imooc.manus.api.application.service;

import com.imooc.manus.api.infrastructure.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 任务准入控制。
 *
 * <p>当前先实现会话级限流与日配额，后续可平滑升级为用户级 / 租户级策略。</p>
 */
@Service
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-28 17:42:39
 */
public class ExecutionAdmissionService {

    private final TaskExecutionService taskExecutionService;
    private final AppProperties appProperties;
    private final Clock clock;
    private final RedisTemplate<String, String> redisTemplate;
    private final ConcurrentMap<String, Deque<Instant>> recentRequests = new ConcurrentHashMap<>();

    @Autowired
    public ExecutionAdmissionService(TaskExecutionService taskExecutionService,
                                     AppProperties appProperties,
                                     @Autowired(required = false) RedisTemplate<String, String> redisTemplate) {
        this(taskExecutionService, appProperties, Clock.systemUTC(), redisTemplate);
    }

    ExecutionAdmissionService(TaskExecutionService taskExecutionService,
                              AppProperties appProperties,
                              Clock clock) {
        this(taskExecutionService, appProperties, clock, null);
    }

    ExecutionAdmissionService(TaskExecutionService taskExecutionService,
                              AppProperties appProperties,
                              Clock clock,
                              RedisTemplate<String, String> redisTemplate) {
        this.taskExecutionService = taskExecutionService;
        this.appProperties = appProperties;
        this.clock = clock;
        this.redisTemplate = redisTemplate;
    }

    public Optional<String> rejectReason(String sessionId) {
        Optional<String> rateLimit = checkRateLimit(sessionId);
        if (rateLimit.isPresent()) {
            return rateLimit;
        }
        Optional<String> activeTaskLimit = checkActiveTaskLimit(sessionId);
        if (activeTaskLimit.isPresent()) {
            return activeTaskLimit;
        }
        return checkDailyQuota(sessionId);
    }

    private Optional<String> checkRateLimit(String sessionId) {
        int limit = appProperties.getAgent().getSessionMessageRatePerMinute();
        if (limit <= 0) {
            return Optional.empty();
        }

        Instant now = clock.instant();
        Optional<String> redisDecision = checkRateLimitWithRedis(sessionId, limit, now);
        if (redisDecision.isPresent()) {
            if ("__accepted_via_redis__".equals(redisDecision.get())) {
                return Optional.empty();
            }
            return redisDecision;
        }

        Instant cutoff = now.minusSeconds(60);
        Deque<Instant> queue = recentRequests.computeIfAbsent(sessionId, key -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst().isBefore(cutoff)) {
                queue.pollFirst();
            }
            if (queue.size() >= limit) {
                return Optional.of("session_rate_limit_exceeded");
            }
            queue.addLast(now);
        }
        return Optional.empty();
    }

    private Optional<String> checkRateLimitWithRedis(String sessionId, int limit, Instant now) {
        if (redisTemplate == null || !StringUtils.hasText(sessionId)) {
            return Optional.empty();
        }
        try {
            Instant windowStart = now.truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            String key = "manus:agent:admission:session-rate:" + sessionId + ":" + windowStart.getEpochSecond();
            Long current = redisTemplate.opsForValue().increment(key);
            if (current != null && current == 1L) {
                redisTemplate.expire(key, Duration.ofMinutes(2));
            }
            if (current != null) {
                return current > limit
                        ? Optional.of("session_rate_limit_exceeded")
                        : Optional.of("__accepted_via_redis__");
            }
        } catch (Exception ignored) {
            // Redis 不可用时自动退回到进程内窗口，避免把准入控制变成系统单点。
        }
        return Optional.empty();
    }

    private Optional<String> checkActiveTaskLimit(String sessionId) {
        int limit = appProperties.getAgent().getSessionActiveTaskLimit();
        if (limit <= 0) {
            return Optional.empty();
        }
        return taskExecutionService.countActiveBySessionId(sessionId) >= limit
                ? Optional.of("session_active_task_limit_exceeded")
                : Optional.empty();
    }

    private Optional<String> checkDailyQuota(String sessionId) {
        int quota = appProperties.getAgent().getSessionTaskQuotaPerDay();
        if (quota <= 0) {
            return Optional.empty();
        }

        LocalDateTime cutoff = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC).minusDays(1);
        long submittedCount = taskExecutionService.countSubmittedSince(sessionId, cutoff);
        if (submittedCount >= quota) {
            return Optional.of("session_daily_task_quota_exceeded");
        }
        return Optional.empty();
    }
}


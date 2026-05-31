package com.imooc.manus.api.application.service;

import com.imooc.manus.api.infrastructure.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionAdmissionServiceTest {

    @Test
    void shouldRejectWhenSessionRateLimitExceeded() {
        TaskExecutionService taskExecutionService = Mockito.mock(TaskExecutionService.class);
        AppProperties properties = new AppProperties();
        properties.getAgent().setSessionMessageRatePerMinute(2);
        properties.getAgent().setSessionTaskQuotaPerDay(0);
        properties.getAgent().setSessionActiveTaskLimit(0);
        Clock clock = Clock.fixed(Instant.parse("2026-05-23T08:00:00Z"), ZoneOffset.UTC);
        ExecutionAdmissionService service = new ExecutionAdmissionService(taskExecutionService, properties, clock);

        assertThat(service.rejectReason("session-1")).isEmpty();
        assertThat(service.rejectReason("session-1")).isEmpty();
        assertThat(service.rejectReason("session-1")).contains("session_rate_limit_exceeded");
    }

    @Test
    void shouldRejectWhenSessionDailyQuotaExceeded() {
        TaskExecutionService taskExecutionService = Mockito.mock(TaskExecutionService.class);
        AppProperties properties = new AppProperties();
        properties.getAgent().setSessionMessageRatePerMinute(0);
        properties.getAgent().setSessionTaskQuotaPerDay(1);
        properties.getAgent().setSessionActiveTaskLimit(0);
        Clock clock = Clock.fixed(Instant.parse("2026-05-23T08:00:00Z"), ZoneOffset.UTC);
        Mockito.when(taskExecutionService.countSubmittedSince(Mockito.eq("session-1"), Mockito.any())).thenReturn(1L);

        ExecutionAdmissionService service = new ExecutionAdmissionService(taskExecutionService, properties, clock);

        assertThat(service.rejectReason("session-1")).contains("session_daily_task_quota_exceeded");
    }

    @Test
    void shouldRejectWhenSessionAlreadyHasActiveTask() {
        TaskExecutionService taskExecutionService = Mockito.mock(TaskExecutionService.class);
        AppProperties properties = new AppProperties();
        properties.getAgent().setSessionMessageRatePerMinute(0);
        properties.getAgent().setSessionTaskQuotaPerDay(0);
        properties.getAgent().setSessionActiveTaskLimit(1);
        Mockito.when(taskExecutionService.countActiveBySessionId("session-1")).thenReturn(1L);

        ExecutionAdmissionService service = new ExecutionAdmissionService(taskExecutionService, properties, Clock.systemUTC());

        assertThat(service.rejectReason("session-1")).contains("session_active_task_limit_exceeded");
    }
}



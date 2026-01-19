package com.paymentservice.config;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ResilienceMetricsConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final io.github.resilience4j.retry.RetryRegistry retryRegistry;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void bindMetrics() {
        // Circuit breaker metrics
        TaggedCircuitBreakerMetrics
            .ofCircuitBreakerRegistry(circuitBreakerRegistry)
            .bindTo(meterRegistry);

        // Retry metrics
        TaggedRetryMetrics
            .ofRetryRegistry(retryRegistry)
            .bindTo(meterRegistry);
    }
}

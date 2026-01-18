package com.productservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {

    /**
     * Circuit Breaker Registry Bean
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .failureRateThreshold(50)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                        org.axonframework.commandhandling.CommandExecutionException.class,
                        org.axonframework.queryhandling.QueryExecutionException.class,
                        java.util.concurrent.TimeoutException.class,
                        java.io.IOException.class
                )
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        // Créer les instances nommées
        registry.circuitBreaker("productCommand", config);
        registry.circuitBreaker("productQuery", CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .failureRateThreshold(50)
                .build()
        );

        return registry;
    }

    /**
     * Retry Registry Bean
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(
                        org.axonframework.commandhandling.CommandExecutionException.class,
                        java.util.concurrent.TimeoutException.class
                )
                .ignoreExceptions(
                        java.lang.IllegalArgumentException.class,
                        org.axonframework.modelling.command.AggregateNotFoundException.class
                )
                .build();

        RetryRegistry registry = RetryRegistry.of(config);

        // Créer les instances nommées
        registry.retry("productCommand", config);
        registry.retry("productQuery", RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(1))
                .build()
        );

        return registry;
    }

    /**
     * Rate Limiter Registry Bean
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);

        // Créer les instances nommées
        registry.rateLimiter("productCommand", config);
        registry.rateLimiter("createProduct", RateLimiterConfig.custom()
                .limitForPeriod(3)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build()
        );
        registry.rateLimiter("productQuery", RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build()
        );

        return registry;
    }

    /**
     * Time Limiter Registry Bean
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .cancelRunningFuture(true)
                .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);

        // Créer les instances nommées
        registry.timeLimiter("productCommand", TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build()
        );
        registry.timeLimiter("productQuery", config);

        return registry;
    }
}
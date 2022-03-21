package io.github.jokoframework.resilience.ratelimiter.interceptor;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Component
@Data
public class RateLimitProps {

    @Value("${joko.resilience.ratelimiter.token.capacity:10}")
    private long tokenCapacity;

    @Value("${joko.resilience.ratelimiter.token.refill:0}")
    private long tokenRefill;

    // By default, we wait 1 minute for token refill
    @Value("${joko.resilience.ratelimiter.token.refill.wait:60}")
    private int refillWaitTime;

    private Duration refillWaitTimeDuration;

    public RateLimitProps() {
        // Empty constructor
    }

    public RateLimitProps(long tokenCapacity, long tokenRefill, int refillWaitTime) {
        this.tokenCapacity = tokenCapacity;
        this.tokenRefill = tokenRefill;
        this.refillWaitTime = refillWaitTime;
        this.computeProps();
    }

    @PostConstruct
    public void computeProps() {
        this.setTokenCapacity(this.getTokenCapacity() <= 0 ? 10 : this.getTokenCapacity());
        // By default, same of capacity
        this.setTokenRefill(this.getTokenRefill() <= 0 ? this.getTokenCapacity() : this.getTokenRefill());
        this.setRefillWaitTime(this.getRefillWaitTime() <= 0 ? 60 : this.getRefillWaitTime());
        this.setRefillWaitTimeDuration(Duration.ofSeconds(refillWaitTime));
    }
}

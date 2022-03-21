package io.github.jokoframework.resilience.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    long tokenCapacity() default 0;

    long tokenRefill() default 0;

    int refillWaitTime() default 0;
}

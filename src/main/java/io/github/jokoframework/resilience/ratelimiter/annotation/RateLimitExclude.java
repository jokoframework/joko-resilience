package io.github.jokoframework.resilience.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimitExclude {

    // The requirement for the Joko Resilience Header and the actual Rate Limiting should not occur if this annotation
    // is set to a request

}

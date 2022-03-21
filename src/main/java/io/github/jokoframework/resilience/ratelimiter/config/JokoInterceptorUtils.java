package io.github.jokoframework.resilience.ratelimiter.config;

import io.github.jokoframework.resilience.ratelimiter.interceptor.JokoGlobalRateLimitInterceptor;
import io.github.jokoframework.resilience.ratelimiter.interceptor.JokoRateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Component
public class JokoInterceptorUtils {

    @Value("${joko.resilience.ratelimiter.interceptor.patterns:/**}")
    private String interceptorPatterns;

    @Value("${joko.resilience.ratelimiter.interceptor.patterns.exclude:}")
    private String excludePatterns;

    @Autowired
    private JokoGlobalRateLimitInterceptor jokoGlobalRateLimitInterceptor;

    @Autowired
    private JokoRateLimitInterceptor jokoRateLimitInterceptor;

    public void addJokoGlobalInterceptor(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(jokoGlobalRateLimitInterceptor).addPathPatterns(interceptorPatterns);
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            interceptorRegistration.excludePathPatterns(excludePatterns);
        }
    }

    public void addJokoInterceptor(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(jokoRateLimitInterceptor).addPathPatterns(interceptorPatterns);
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            interceptorRegistration.excludePathPatterns(excludePatterns);
        }
    }
}

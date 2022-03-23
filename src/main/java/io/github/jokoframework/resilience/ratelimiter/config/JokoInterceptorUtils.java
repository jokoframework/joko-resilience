package io.github.jokoframework.resilience.ratelimiter.config;

import io.github.jokoframework.resilience.ratelimiter.annotation.RateLimit;
import io.github.jokoframework.resilience.ratelimiter.interceptor.JokoGlobalRateLimitInterceptor;
import io.github.jokoframework.resilience.ratelimiter.interceptor.JokoRateLimitInterceptor;
import io.github.jokoframework.resilience.ratelimiter.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
@ComponentScan("io.github.jokoframework.resilience.ratelimiter")
public class JokoInterceptorUtils {

    @Value("${joko.resilience.ratelimiter.interceptor.patterns:/**}")
    private String interceptorPatterns;

    @Value("${joko.resilience.ratelimiter.interceptor.patterns.exclude:}")
    private String excludePatterns;

    @Value("${joko.resilience.ratelimiter.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${joko.resilience.ratelimiter.resources.exclude:true}")
    private boolean excludeResources;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private JokoGlobalRateLimitInterceptor jokoGlobalRateLimitInterceptor;

    @Autowired
    private JokoRateLimitInterceptor jokoRateLimitInterceptor;

    public void addJokoGlobalRateLimitInterceptor(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(jokoGlobalRateLimitInterceptor).addPathPatterns(interceptorPatterns);
        configExclusions(interceptorRegistration);
    }

    public void addJokoRateLimitInterceptor(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(jokoRateLimitInterceptor).addPathPatterns(interceptorPatterns);
        configExclusions(interceptorRegistration);
    }

    public void configExclusions(InterceptorRegistration interceptorRegistration) {
        List<String> exclusions = new ArrayList<>();
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            String[] patternsSplit = excludePatterns.split(",");
            for (String exclusion : patternsSplit) {
                exclusions.add(exclusion.trim());
            }
            interceptorRegistration.excludePathPatterns(exclusions);
        }
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler, boolean global)
            throws IOException {
        // We ignore requests that handle static elements like js and css resources
        if (excludeResources && handler instanceof ResourceHttpRequestHandler) {
            return true;
        }
        Method method = ((HandlerMethod) handler).getMethod();

        // If annotation is present and rate limit is enabled we execute the rate limiting logic
        if (global) {
            return !method.isAnnotationPresent(RateLimit.class) && this.rateLimitEnabled
                    ? this.rateLimiterService.addRateLimit(request, response, method) : true;
        }
        return method.isAnnotationPresent(RateLimit.class) && this.rateLimitEnabled
                ? this.rateLimiterService.addRateLimit(request, response, method) : true;
    }
}

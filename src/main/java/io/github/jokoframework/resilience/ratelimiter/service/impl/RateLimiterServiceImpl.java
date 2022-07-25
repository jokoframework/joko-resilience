package io.github.jokoframework.resilience.ratelimiter.service.impl;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import io.github.jokoframework.resilience.ratelimiter.annotation.RateLimit;
import io.github.jokoframework.resilience.ratelimiter.interceptor.RateLimitProps;
import io.github.jokoframework.resilience.ratelimiter.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    public static final String X_RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    public static final String RATE_LIMIT_RETRY_AFTER_SECONDS = "Rate-Limit-Retry-After-Seconds";
    public static final String JSESSIONID = "JSESSIONID";
    public static final String MISSING_HEADER = "Missing Header: ";

    // Cache for API key and token bucket
    Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Autowired
    private RateLimitProps rateLimitGlobalProps;

    private RateLimitProps rateLimitMergedProps;

    @Value("${joko.resilience.ratelimiter.auth.header-name:Authorization}")
    private String authHeader;

    @Value("${joko.resilience.ratelimiter.auth.jsession.cookie.enabled:false}")
    private boolean jSessionCookieEnabled;

    @Value("${joko.resilience.ratelimiter.error.message:You have exhausted your API Request Quota}")
    private String errorMessage;

    @Override
    public Bucket resolveBucket(String apiKey) {
        return bucketCache.computeIfAbsent(apiKey, this::newBucket);
    }

    private Bucket newBucket(String s) {
        if (this.rateLimitMergedProps == null) {
            this.rateLimitMergedProps = this.rateLimitGlobalProps;
        }
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        this.rateLimitMergedProps.getTokenCapacity(),
                        Refill.intervally(this.rateLimitMergedProps.getTokenRefill(),
                                this.rateLimitMergedProps.getRefillWaitTimeDuration())))
                .build();
    }

    private String getApiKey(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String apiKey;
        if (jSessionCookieEnabled) {
            Cookie jSessionCookie = WebUtils.getCookie(request, JSESSIONID);
            apiKey = jSessionCookie != null ? jSessionCookie.getValue() : "";
        } else {
            apiKey = request.getHeader(authHeader);
            if (apiKey == null || apiKey.isEmpty()) {
                String exceptionMessage = MISSING_HEADER + authHeader;
                response.sendError(HttpStatus.BAD_REQUEST.value(), exceptionMessage);
                throw new IOException(exceptionMessage);
            }
        }
        return apiKey;
    }

    public boolean addRateLimit(HttpServletRequest request, HttpServletResponse response, Method method) throws IOException {
        Annotation annotation = method.getAnnotation(RateLimit.class);
        String methodName = method.getName();
        String apiKey = this.getApiKey(request, response);
        String endpointUrl = this.getEndpointUrl(method, request);
        String bucketKey = apiKey + methodName + endpointUrl;
        this.configureProps((RateLimit) annotation);
        Bucket bucket = this.resolveBucket(bucketKey);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader(X_RATE_LIMIT_REMAINING, String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader(RATE_LIMIT_RETRY_AFTER_SECONDS, String.valueOf(waitForRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), this.errorMessage);
            return false;
        }
    }

    private RateLimitProps getMergedProps(RateLimit annotation) {
        return new RateLimitProps(
                annotation.tokenCapacity() <= 0 ? this.rateLimitGlobalProps.getTokenCapacity() : annotation.tokenCapacity(),
                annotation.tokenRefill() <= 0 ? this.rateLimitGlobalProps.getTokenRefill() : annotation.tokenRefill(),
                annotation.refillWaitTime() <= 0 ? this.rateLimitGlobalProps.getRefillWaitTime() : annotation.refillWaitTime());
    }


    private void configureProps(RateLimit annotation) {
        this.rateLimitMergedProps = annotation != null ? this.getMergedProps(annotation) : this.rateLimitGlobalProps;
    }

    private String getEndpointUrl(Method method, HttpServletRequest request) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return method.getAnnotation(GetMapping.class).value()[0];
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return method.getAnnotation(PutMapping.class).value()[0];
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return method.getAnnotation(PostMapping.class).value()[0];
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            return method.getAnnotation(PatchMapping.class).value()[0];
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return method.getAnnotation(DeleteMapping.class).value()[0];
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            return method.getAnnotation(RequestMapping.class).value()[0];
        } else if(request.getContextPath() != null && !request.getContextPath().isEmpty()){
            return request.getContextPath();
        } else return "";
    }
}

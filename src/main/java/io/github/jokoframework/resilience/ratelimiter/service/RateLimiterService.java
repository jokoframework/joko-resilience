package io.github.jokoframework.resilience.ratelimiter.service;

import io.github.bucket4j.Bucket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

public interface RateLimiterService {

    Bucket resolveBucket(String apiKey);

    boolean addRateLimit(HttpServletRequest request, HttpServletResponse response, Method method) throws IOException;

}

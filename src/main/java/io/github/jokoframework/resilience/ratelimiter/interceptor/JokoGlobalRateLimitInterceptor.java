package io.github.jokoframework.resilience.ratelimiter.interceptor;

import io.github.jokoframework.resilience.ratelimiter.annotation.RateLimit;
import io.github.jokoframework.resilience.ratelimiter.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class JokoGlobalRateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Value("${joko.resilience.ratelimiter.enabled:true}")
    private boolean rateLimitEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Method method = ((HandlerMethod) handler).getMethod();

        // For global interceptor we ignore methods annotated with RateLimit annotation
        // If rate limit is enabled we execute the rate limit logic
        return !method.isAnnotationPresent(RateLimit.class) && this.rateLimitEnabled
                ? this.rateLimiterService.addRateLimit(request, response, method) : true;
    }


    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //Do nothing
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //Do nothing
    }
}

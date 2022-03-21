package io.github.jokoframework.resilience.ratelimiter.aspect;

import io.github.jokoframework.resilience.ratelimiter.annotation.RateLimitInterceptor;
import io.github.jokoframework.resilience.ratelimiter.interceptor.JokoGlobalRateLimitInterceptor;
import io.github.jokoframework.resilience.ratelimiter.interceptor.JokoRateLimitInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.lang.reflect.Method;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private JokoRateLimitInterceptor jokoRateLimitInterceptor;

    @Autowired
    private JokoGlobalRateLimitInterceptor jokoGlobalRateLimitInterceptor;

    @Value("${joko.resilience.ratelimiter.enabled:true}")
    private boolean rateLimitEnabled;

    @Around("@annotation(io.github.jokoframework.resilience.ratelimiter.annotation.RateLimitInterceptor)")
    public Object addRateLimitInterceptor(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        RateLimitInterceptor annotation = method.getAnnotation(RateLimitInterceptor.class);
        Object[] args = proceedingJoinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof InterceptorRegistry && rateLimitEnabled) {
            InterceptorRegistry interceptorRegistry = (InterceptorRegistry) args[0];
            if (annotation.global()) {
                interceptorRegistry.addInterceptor(jokoGlobalRateLimitInterceptor).addPathPatterns(annotation.pattern());
            } else {
                interceptorRegistry.addInterceptor(jokoRateLimitInterceptor).addPathPatterns(annotation.pattern());
            }
        }
        return proceedingJoinPoint.proceed();
    }

}

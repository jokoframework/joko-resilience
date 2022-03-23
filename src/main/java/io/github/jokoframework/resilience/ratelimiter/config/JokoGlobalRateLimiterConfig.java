package io.github.jokoframework.resilience.ratelimiter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ComponentScan("io.github.jokoframework.resilience.ratelimiter.config")
public class JokoGlobalRateLimiterConfig implements WebMvcConfigurer {

    @Autowired
    private JokoInterceptorUtils jokoInterceptorUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        jokoInterceptorUtils.addJokoGlobalRateLimitInterceptor(registry);
    }

}

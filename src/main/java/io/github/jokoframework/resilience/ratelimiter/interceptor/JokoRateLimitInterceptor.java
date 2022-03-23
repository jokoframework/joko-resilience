package io.github.jokoframework.resilience.ratelimiter.interceptor;

import io.github.jokoframework.resilience.ratelimiter.config.JokoInterceptorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JokoRateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private JokoInterceptorUtils jokoInterceptorUtils;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return jokoInterceptorUtils.preHandle(request, response, handler, false);
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

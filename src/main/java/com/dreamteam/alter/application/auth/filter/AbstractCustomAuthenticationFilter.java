package com.dreamteam.alter.application.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

public abstract class AbstractCustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    protected final AuthenticationEntryPoint authenticationEntryPoint;

    public AbstractCustomAuthenticationFilter(
        RequestMatcher requestMatcher,
        AuthenticationManager authenticationManager,
        AuthenticationEntryPoint authenticationEntryPoint
    ) {
        super(requestMatcher);
        // 인증 성공 후 강제 redirection을 막음
        setAuthenticationSuccessHandler(new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(
                HttpServletRequest request,
                HttpServletResponse response,
                Authentication authentication
            ) throws IOException, ServletException {
                // ignore
            }
        });
        setAuthenticationManager(authenticationManager);
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
                                                                                              IOException,
                                                                                              ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
                                                                                                         IOException,
                                                                                                         ServletException {
        if (BooleanUtils.isFalse(requiresAuthentication(request, response))) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = attemptAuthentication(request, response);
            if (ObjectUtils.isNotEmpty(authentication) && authentication.isAuthenticated()) {
                SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
            }
            chain.doFilter(request, response);
            successfulAuthentication(request, response, chain, authentication);
        } catch (InternalAuthenticationServiceException ex) {
            unsuccessfulAuthentication(request, response, ex);
        }
    }

}

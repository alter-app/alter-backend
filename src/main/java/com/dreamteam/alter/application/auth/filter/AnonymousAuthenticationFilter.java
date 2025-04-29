package com.dreamteam.alter.application.auth.filter;

import com.dreamteam.alter.application.auth.entrypoint.CustomAuthenticationEntryPoint;
import com.dreamteam.alter.application.auth.token.AnonymousAuthentication;
import com.dreamteam.alter.domain.auth.exception.InternalAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

public class AnonymousAuthenticationFilter extends AbstractCustomAuthenticationFilter {

    public AnonymousAuthenticationFilter(
        AuthenticationManager authenticationManager,
        CustomAuthenticationEntryPoint authenticationEntryPoint
    ) {
        super(request -> true, authenticationManager, authenticationEntryPoint);
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
                                                                                                         IOException,
                                                                                                         ServletException {
        if (BooleanUtils.isFalse(requiresAuthentication(request, response))) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Authentication preAuthentication = SecurityContextHolder.getContext().getAuthentication();
            if (ObjectUtils.isNotEmpty(preAuthentication) && preAuthentication.isAuthenticated()) {
                chain.doFilter(request, response);
                return;
            }

            Authentication authentication = attemptAuthentication(request, response);
            if (authentication.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(request, response);
        } catch (InternalAuthenticationServiceException ex) {
            unsuccessfulAuthentication(request, response, ex);
        } catch (AuthenticationException ex) {
            throw new InternalAuthException();
        }

    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        AnonymousAuthentication authenticationToken = new AnonymousAuthentication();

        return getAuthenticationManager().authenticate(authenticationToken);
    }

}

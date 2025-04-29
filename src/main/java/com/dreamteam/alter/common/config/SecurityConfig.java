package com.dreamteam.alter.common.config;

import com.dreamteam.alter.application.auth.entrypoint.CustomAccessDeniedHandler;
import com.dreamteam.alter.application.auth.entrypoint.CustomAuthenticationEntryPoint;
import com.dreamteam.alter.application.auth.filter.AccessTokenAuthenticationFilter;
import com.dreamteam.alter.application.auth.filter.AnonymousAuthenticationFilter;
import com.dreamteam.alter.application.auth.filter.RefreshTokenAuthenticationFilter;
import com.dreamteam.alter.application.auth.provider.AccessTokenAuthenticationProvider;
import com.dreamteam.alter.application.auth.provider.AnonymousAuthenticationProvider;
import com.dreamteam.alter.application.auth.provider.RefreshTokenAuthenticationProvider;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${alter.security.allowed-origin-patterns}")
    private List<String> allowedOriginPatterns;

    @Value("${alter.security.allowed-methods}")
    private List<String> allowedMethods;

    @Value("${alter.security.allowed-headers}")
    private List<String> allowedHeaders;

    @Value("${alter.security.exposed-headers}")
    private List<String> exposedHeaders;

    @Value("${alter.security.allowed-pattern}")
    private String allowedPattern;

    @Value("${alter.security.permit-all-urls}")
    private String[] permitAllUrls;

    private final AccessTokenAuthenticationProvider accessTokenAuthenticationProvider;
    private final RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider;
    private final AnonymousAuthenticationProvider anonymousAuthenticationProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Resource
    public void configure(AuthenticationManagerBuilder managerBuilder) {
        managerBuilder.authenticationProvider(accessTokenAuthenticationProvider);
        managerBuilder.authenticationProvider(refreshTokenAuthenticationProvider);
        managerBuilder.authenticationProvider(anonymousAuthenticationProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setExposedHeaders(exposedHeaders);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(allowedPattern, configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();

        return http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .anonymous(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .rememberMe(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(permitAllUrls)
                .permitAll()
                .anyRequest()
                .authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(handler -> handler
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .addFilterBefore(accessTokenAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(refreshTokenAuthenticationFilter(authenticationManager), AccessTokenAuthenticationFilter.class)
            .addFilterAfter(anonymousAuthenticationFilter(authenticationManager), RefreshTokenAuthenticationFilter.class)
            .build();
    }

    public AccessTokenAuthenticationFilter accessTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new AccessTokenAuthenticationFilter(authenticationManager, customAuthenticationEntryPoint);
    }

    public RefreshTokenAuthenticationFilter refreshTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new RefreshTokenAuthenticationFilter(authenticationManager, customAuthenticationEntryPoint);
    }

    public AnonymousAuthenticationFilter anonymousAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new AnonymousAuthenticationFilter(authenticationManager, customAuthenticationEntryPoint);
    }

}

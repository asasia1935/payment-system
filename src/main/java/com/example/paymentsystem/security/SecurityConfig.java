package com.example.paymentsystem.security;

import com.example.paymentsystem.apikey.ApiKeyAuthenticationFilter;
import com.example.paymentsystem.apikey.ApiKeyAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    @Order(1)
    SecurityFilterChain paymentApiChain(HttpSecurity http,
                                        ApiKeyAuthenticationProvider provider) throws Exception {
        AuthenticationManager manager = new ProviderManager(provider);
        http.securityMatcher("/api/payments", "/api/payments/**")
                .csrf(csrf -> csrf.disable()) // Key-based, stateless API; no browser session cookie
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(c -> c.disable())
                .formLogin(c -> c.disable())
                .httpBasic(c -> c.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/payments").hasAuthority("PAYMENT_CREATE")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/payments/*").hasAuthority("PAYMENT_READ")
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(401, "Authentication required"))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(403, "Access denied")))
                .addFilterBefore(new ApiKeyAuthenticationFilter(manager), AnonymousAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Developer/admin account authentication is not implemented yet.
     * Never silently expose the pre-existing /api/merchants routes.
     */
    @Bean
    @Order(2)
    SecurityFilterChain defaultDenyAll(HttpSecurity http) throws Exception {
        http.formLogin(c -> c.disable())
                .httpBasic(c -> c.disable())
                .authorizeHttpRequests(a -> a.anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(401, "Authentication required"))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(403, "Access denied")));
        return http.build();
    }
}

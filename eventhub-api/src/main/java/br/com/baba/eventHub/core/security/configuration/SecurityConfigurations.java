package br.com.baba.eventHub.core.security.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfigurations {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return
                http.csrf(csrf -> csrf.disable())
                        .headers(headers -> headers
                                .httpStrictTransportSecurity(hsts -> hsts
                                        .requestMatcher(AnyRequestMatcher.INSTANCE)
                                        .includeSubDomains(true)
                                        .maxAgeInSeconds(31536000))
                                .contentSecurityPolicy(csp -> csp.policyDirectives(
                                        "default-src 'self'; script-src 'self' 'unsafe-inline'; "
                                                + "style-src 'self' 'unsafe-inline'; img-src 'self' data:; "
                                                + "frame-ancestors 'none'"))
                                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER)))
                        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(req -> {
                            req.requestMatchers(HttpMethod.POST, "/login").permitAll();
                            req.requestMatchers(HttpMethod.POST, "/api/users").permitAll();
                            req.requestMatchers(HttpMethod.GET, "/api/events").permitAll();
                            req.requestMatchers(HttpMethod.GET, "/actuator/health").permitAll();
                            req.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
                            req.anyRequest().authenticated();
                        })
                        .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                        .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

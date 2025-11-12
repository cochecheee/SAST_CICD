package com.nhoclahola.socialnetworkv1.configuration;

import com.nhoclahola.socialnetworkv1.exception.FilterExceptionHandler;
import com.nhoclahola.socialnetworkv1.exception.JwtAuthenticationEntryPoint;
import com.nhoclahola.socialnetworkv1.security.JwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig
{
    @NonFinal
    @Value("${cors.allowed.origins}")
    private String[] allowedOrigins;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception
    {
        httpSecurity.authorizeRequests(request ->
                request.antMatchers("/api/admin/**").hasRole("ADMIN")
                        .antMatchers("/api/**").authenticated()
                        .anyRequest().permitAll());

        httpSecurity.sessionManagement(management ->
                management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity.addFilterBefore(new JwtValidator(), BasicAuthenticationFilter.class);
        httpSecurity.addFilterBefore(new FilterExceptionHandler(), JwtValidator.class);
        httpSecurity.exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        httpSecurity.csrf().disable();
        httpSecurity.cors().configurationSource(corsConfigurationSource());
        return httpSecurity.build();

    }

    private CorsConfigurationSource corsConfigurationSource()
    {
        // Interface with 1 method
        return request ->
        {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(List.of(allowedOrigins));
            configuration.setAllowedMethods(List.of("*"));
            configuration.setAllowCredentials(true);
            configuration.setAllowedHeaders(List.of("*"));
            configuration.setExposedHeaders(List.of("Authorization"));
            configuration.setMaxAge(3600L);
            return configuration;
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}

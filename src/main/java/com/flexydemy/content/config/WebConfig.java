package com.flexydemy.content.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Order
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebConfig {


    @Autowired
    private JwtDecoderService jwtDecoderService;

    private static final String[] AUTH_LIST = {
            "/dev/cnt/api/v1/tutors/create",
            "/cnt/api/v1/tutors/create",
            "/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/dev/cnt/actuator/**",
            "/cnt/actuator/**",

    };
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("http://localhost:4000", "http://localhost:3000", "https://dev.flexydemy.com:4000","https://dev.flexydemy.com","https://flexydemy.com") // 👈 use patterns instead of '*'
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true); // ✅ now valid
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeRequests().requestMatchers(AUTH_LIST).permitAll()
                .requestMatchers("/cnt/**", "/dev/cnt/**", "/dev/autocomplete/**","/organization").authenticated().and()
                .addFilterBefore(new AuthenticationFilter(jwtDecoderService), UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())  // Disable HTTP Basic authentication
                .csrf(csrf -> csrf.disable())
                .headers(header -> header.cacheControl(HeadersConfigurer.CacheControlConfig::disable))// Disable CSRF protection
                .sessionManagement(mgt -> mgt.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .anonymous(anonymous -> anonymous.disable()); // Disable anonymous access
        return http.build();
    }
}

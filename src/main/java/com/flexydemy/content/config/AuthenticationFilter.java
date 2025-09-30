package com.flexydemy.content.config;

import com.flexydemy.content.exceptions.InvalidJwtAuthenticationException;
import com.flexydemy.content.model.ProfileRole;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoderService jwtDecoderService;

    public AuthenticationFilter(JwtDecoderService jwtDecoderService) {
        this.jwtDecoderService = jwtDecoderService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String path = request.getRequestURI();


        if (jwtDecoderService.isPublicPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtDecoderService.isTokenValid(token)) {
                Authentication authentication = buildAuthenticationFromToken(token);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                throw new InvalidJwtAuthenticationException();
            }
        } else {
            throw new InvalidJwtAuthenticationException();
        }

        filterChain.doFilter(request, response);
    }

    private Authentication buildAuthenticationFromToken(String token) {
        Claims decodedClaims = jwtDecoderService.extractAllClaims(token);

        String username = decodedClaims.getSubject();
        String userId = String.valueOf(decodedClaims.get("userId"));
        List<String> roles = decodedClaims.get("roles", List.class); // expects ["ADMIN", "USER", ...]

        if (username == null || roles == null) {
            return null;
        }

        // Convert roles into GrantedAuthority with "ROLE_" prefix
        Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // Optional: build internal ProfileRole enum
        Set<ProfileRole> profileRoles = roles.stream()
                .map(ProfileRole::valueOf)
                .collect(Collectors.toSet());

        // Build custom UserDetails
        UserDetails details = new UserDetails();
        details.setEmail(username);
        details.setUserId(userId);
        details.setRoles(profileRoles);

        return new UsernamePasswordAuthenticationToken(details, null, authorities);
    }
}

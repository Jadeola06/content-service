package com.flexydemy.content.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class JwtDecoderService {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/dev/cnt/api/v1/tutors/create",
            "/dev/cnt/api/v1/tutors/create",
            "/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars",
            "/dev/cnt/actuator",
            "/cnt/actuator"
    );
    private PublicKey publicKey;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream certStream = new ClassPathResource("cert/public-cert.pem").getInputStream();
            X509Certificate certificate = (X509Certificate) factory.generateCertificate(certStream);
            this.publicKey = certificate.getPublicKey();
            this.jwtParser = Jwts.parserBuilder().setSigningKey(publicKey).build();
            log.info("Public key loaded successfully for JWT decoding.");
        } catch (Exception e) {
            throw new RuntimeException("Unable to load public key from PEM file", e);
        }
    }
    public Claims extractAllClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isPublicPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(uri::startsWith);
        System.out.println("Checking isPublicPath for URI: " + uri + " → " + isPublic);
        return isPublic;
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }
}

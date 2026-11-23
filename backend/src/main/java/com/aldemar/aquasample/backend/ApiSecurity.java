package com.aldemar.aquasample.backend;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ApiSecurity {
    @Bean
    JwtDecoder jwtDecoder(@Value("${aquasample.firebase-project:aquasample-bc64b}") String project) {
        var decoder = NimbusJwtDecoder.withJwkSetUri(
            "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com").build();
        OAuth2TokenValidator<Jwt> identity = jwt ->
            jwt.getAudience().contains(project) && jwt.getSubject() != null && !jwt.getSubject().isBlank()
                && jwt.getExpiresAt() != null && jwt.getIssuedAt() != null
                && !jwt.getIssuedAt().isAfter(java.time.Instant.now().plusSeconds(60))
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer("https://securetoken.google.com/" + project), identity));
        return decoder;
    }

    @Bean
    SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/samples/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/samples/*/review").hasRole("SUPERVISOR")
                .requestMatchers(HttpMethod.POST, "/api/samples").hasAnyRole("OPERATOR", "SUPERVISOR")
                .requestMatchers(HttpMethod.PUT, "/api/samples/*").hasAnyRole("OPERATOR", "SUPERVISOR")
                .anyRequest().denyAll())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwt -> {
                String claim = jwt.getClaimAsString("role");
                String role = "SUPERVISOR".equals(claim) || "ANALYST".equals(claim) ? claim : "OPERATOR";
                return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            }))).build();
    }
}

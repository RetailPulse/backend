package com.retailpulse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * @Author WilliamSiling
 * @create 5/2/2025 3:38 pm
 */
@Configuration
public class RetailPulseConfig {

  @Value("${auth.jwt.key.set.uri}")
  private String keySetUri;

  @Value("${auth.enabled}")
  private boolean authEnabled;

  @Value("${auth.origin}")
  private String originURL;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    if (authEnabled) {
      System.out.println("Auth enabled");
      http.oauth2ResourceServer(
          c -> c.jwt(
            j -> j.jwkSetUri(keySetUri).jwtAuthenticationConverter(jwtAuthenticationConverter())
          )
      );

      http.authorizeHttpRequests(
        c -> c.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
          .requestMatchers("/hello").authenticated()
          .requestMatchers("/api/**").authenticated() //.hasRole("SUPER").anyRequest().authenticated()
      );
    } else {
      System.out.println("No auth enabled");
      http.authorizeHttpRequests(        
        c -> c.anyRequest().permitAll()
      );
    }

    http.cors(c -> {
      c.configurationSource(corsConfigurationSource());
    });

    http.csrf(csrf -> csrf.disable());
    return http.build();
  }

  private CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(originURL));
    configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setExposedHeaders(List.of("Authorization"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
    return jwtAuthenticationConverter;
  }

  private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
    return jwtGrantedAuthoritiesConverter;
  }
}

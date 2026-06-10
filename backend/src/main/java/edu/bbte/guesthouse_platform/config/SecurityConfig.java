package edu.bbte.guesthouse_platform.config;

import edu.bbte.guesthouse_platform.shared.api.ApiPaths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, ApiPaths.PUBLIC + "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, ApiPaths.PUBLIC + "/bookings/requests").permitAll()
                        .requestMatchers(HttpMethod.POST, ApiPaths.PUBLIC + "/itinerary/recommendations").permitAll()
                        .requestMatchers(HttpMethod.POST, ApiPaths.ADMIN + "/auth/login").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}

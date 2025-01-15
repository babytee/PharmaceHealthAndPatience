package com.pharmacy.intelrx.config;

import com.pharmacy.intelrx.auxilliary.services.LogoutService;
import com.pharmacy.intelrx.common.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static com.pharmacy.intelrx.auxilliary.models.Permission.*;
import static com.pharmacy.intelrx.auxilliary.models.Role.*;
import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())  // Enable CORS
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/v1/auth/logout",
                                "/api/v1/AuthenticationService/auth/**",
                                "/api/v1/test/**",
                                "/api/v1/**",
                                "/api/v1/admin/auth/**",    // Admin authentication paths
                                "/api/v1/user/auth/**"    // User authentication paths
                                //"/api/v1/admin/**",
                                //"/api/v1/user/**"
                        )
                        .permitAll()
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole(ADMIN.name())
                        .requestMatchers(GET, "/api/v1/admin/**")
                        .hasAuthority(ADMIN_READ.name())
                        .requestMatchers(POST, "/api/v1/admin/**")
                        .hasAuthority(ADMIN_CREATE.name())
                        .requestMatchers(PUT, "/api/v1/admin/**")
                        .hasAuthority(ADMIN_UPDATE.name())
                        .requestMatchers(DELETE, "/api/v1/admin/**")
                        .hasAuthority(ADMIN_DELETE.name())

                        .requestMatchers("/api/v1/user/**")
                        .hasRole(USER.name())
                        .requestMatchers(GET, "/api/v1/user/**")
                        .hasAuthority(USER_READ.name())
                        .requestMatchers(POST, "/api/v1/user/**")
                        .hasAuthority(USER_CREATE.name())
                        .requestMatchers(PUT, "/api/v1/user/**")
                        .hasAuthority(USER_UPDATE.name())
                        .requestMatchers(DELETE, "/api/v1/user/**")
                        .hasAuthority(USER_DELETE.name())
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(customAccessDeniedHandler) // Inject custom access denied handler
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                            // You can add additional logic here if needed
                        })
                );

        http.sessionManagement(sessionAuthenticationStrategy -> {
            sessionAuthenticationStrategy.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });

        http.authenticationProvider(authenticationProvider);

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(false);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.addAllowedHeader("*");
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

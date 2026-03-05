package com.example.theatre.config;

import com.example.theatre.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private BannedUserAuthenticationFailureHandler bannedUserAuthenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Отключаем CSRF для REST API (можно использовать JWT в будущем)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/style.css", "/about").permitAll()
                .requestMatchers("/account/**").authenticated() // Доступ к аккаунту только для аутентифицированных пользователей
                .requestMatchers("/plays/**", "/performances/**", "/tickets/**", "/statistics").hasAnyRole("OWNER", "ADMIN", "USER")
                .requestMatchers("/plays/edit/**", "/plays/delete/**", "/plays/new", 
                    "/performances/edit/**", "/performances/delete/**", "/performances/new").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/admin/**").hasAnyRole("OWNER", "ADMIN") // Доступ для OWNER и ADMIN, дополнительная проверка в контроллере
                .requestMatchers("/api/plays/**", "/api/statistics").hasAnyRole("OWNER", "ADMIN", "USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/plays", true)
                .failureHandler(bannedUserAuthenticationFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }
}


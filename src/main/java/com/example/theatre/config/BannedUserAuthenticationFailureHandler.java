package com.example.theatre.config;

import com.example.theatre.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Обработчик ошибок аутентификации для забаненных пользователей
 */
@Component
public class BannedUserAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    public BannedUserAuthenticationFailureHandler() {
        super.setDefaultFailureUrl("/login?error=true");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        String username = request.getParameter("username");
        boolean isBanned = false;
        
        if (username != null) {
            isBanned = userRepository.findByUsername(username)
                .map(user -> Boolean.TRUE.equals(user.getBanned()))
                .orElse(false);
        }
        
        // Проверяем, была ли ошибка из-за бана (accountNonLocked = false)
        if (isBanned || (exception.getMessage() != null && 
            (exception.getMessage().contains("account is disabled") || 
             exception.getMessage().contains("account is locked") ||
             exception.getMessage().contains("accountNonLocked")))) {
            setDefaultFailureUrl("/login?banned=true");
        } else {
            setDefaultFailureUrl("/login?error=true");
        }
        
        super.onAuthenticationFailure(request, response, exception);
    }
}


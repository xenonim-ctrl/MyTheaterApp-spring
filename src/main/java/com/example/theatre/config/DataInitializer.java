package com.example.theatre.config;

import com.example.theatre.model.Role;
import com.example.theatre.model.User;
import com.example.theatre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Убеждаемся, что все существующие пользователи имеют поле banned установленным
        userRepository.findAll().forEach(user -> {
            if (user.getBanned() == null) {
                user.setBanned(false);
                userRepository.save(user);
            }
            if (user.getIsRootAdmin() == null) {
                user.setIsRootAdmin(false);
                userRepository.save(user);
            }
        });
        
        // Проверяем, существует ли OWNER в системе (должен быть ровно один)
        long ownerCount = userRepository.countByRole(Role.OWNER);
        if (ownerCount > 1) {
            System.err.println("ВНИМАНИЕ: Обнаружено более одного пользователя с ролью OWNER! Ожидается только один OWNER.");
        } else if (ownerCount == 1) {
            System.out.println("OWNER найден в базе данных. OWNER сохраняется без изменений.");
        }
        
        // Проверяем, существует ли хотя бы один root администратор
        List<User> allAdmins = userRepository.findAll().stream()
                .filter(user -> Boolean.TRUE.equals(user.getIsRootAdmin()))
                .filter(user -> user.getRole() != Role.OWNER) // OWNER не считается root admin в этом контексте
                .toList();
        
        // Если root администратор не существует, создаем его (но не создаем OWNER)
        if (allAdmins.isEmpty()) {
            // Проверяем, существует ли пользователь "admino" (старый логин)
            Optional<User> existingAdmino = userRepository.findByUsername("admino");
            
            if (existingAdmino.isPresent()) {
                User admin = existingAdmino.get();
                // Если это не OWNER, делаем его root администратором
                if (admin.getRole() != Role.OWNER) {
                    admin.setIsRootAdmin(true);
                    if (admin.getRole() != Role.ADMIN) {
                        admin.setRole(Role.ADMIN);
                    }
                    userRepository.save(admin);
                    System.out.println("Пользователь 'admino' назначен root администратором.");
                } else {
                    System.out.println("Пользователь 'admino' является OWNER. Пропускаем изменение.");
                }
            } else {
                // Создаем нового root администратора только если его нет (не создаем OWNER)
                User admin = new User();
                admin.setUsername("admino");
                admin.setPassword(passwordEncoder.encode("meadmin123"));
                admin.setRole(Role.ADMIN); // Не OWNER!
                admin.setFullName("Фортуна");
                admin.setPhone("+79999999999");
                admin.setEmail("admin@theater.com");
                admin.setBanned(false);
                admin.setIsRootAdmin(true);
                userRepository.save(admin);
                System.out.println("Администратор по умолчанию создан: admino / meadmin123");
                System.out.println("Имя: Фортуна, Роль: ADMIN, Root Admin: true");
            }
        } else {
            System.out.println("Root администратор уже существует. Пропускаем создание по умолчанию.");
        }
    }
}


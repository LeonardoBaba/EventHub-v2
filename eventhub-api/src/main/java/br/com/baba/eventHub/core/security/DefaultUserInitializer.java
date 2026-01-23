package br.com.baba.eventHub.core.security;

import br.com.baba.eventHub.core.dto.UserFormDTO;
import br.com.baba.eventHub.core.enums.UserRoleEnum;
import br.com.baba.eventHub.core.exceptions.UserException;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.UserRepository;
import br.com.baba.eventHub.core.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostConstruct
    public void initializeDefaultUser() throws UserException {
        if (userRepository.findAll().isEmpty()) {
            User user = userService.createUser(new UserFormDTO(
                    "Admin",
                    "admin@admin.com",
                    "12345678901",
                    "123456"
            ));
            user.setRole(UserRoleEnum.ADMIN);
            userRepository.save(user);
        }
    }
}

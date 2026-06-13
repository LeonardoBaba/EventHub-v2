package br.com.baba.eventHub.core.security;

import br.com.baba.eventHub.core.dto.UserFormDTO;
import br.com.baba.eventHub.core.enums.UserRoleEnum;
import br.com.baba.eventHub.core.exceptions.UserException;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.UserRepository;
import br.com.baba.eventHub.core.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Value("${admin.credentials:}")
    private String adminCredentials;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.cpf:}")
    private String adminCpf;

    @PostConstruct
    public void initializeDefaultUser() throws UserException {
        if (!userRepository.findAll().isEmpty()) {
            return;
        }

        if (adminCredentials.isBlank() || adminEmail.isBlank() || adminCpf.isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_CREDENTIALS, ADMIN_EMAIL and ADMIN_CPF must be set to bootstrap the default admin user");
        }

        String[] parts = adminCredentials.split(":", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalStateException("ADMIN_CREDENTIALS must be in the format 'name:password'");
        }

        User user = userService.createUser(new UserFormDTO(parts[0], adminEmail, adminCpf, parts[1]));
        user.setRole(UserRoleEnum.ADMIN);
        userRepository.save(user);
    }
}

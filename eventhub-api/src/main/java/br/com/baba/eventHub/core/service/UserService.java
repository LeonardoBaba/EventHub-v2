package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.dto.UserFormDTO;
import br.com.baba.eventHub.core.dto.UserResponseDTO;
import br.com.baba.eventHub.core.exceptions.UserAlreadyExistsException;
import br.com.baba.eventHub.core.exceptions.UserException;
import br.com.baba.eventHub.core.exceptions.UserNotFoundException;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.UserRepository;
import br.com.baba.eventHub.core.security.configuration.SecurityConfigurations;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfigurations securityConfigurations;

    @Transactional
    public User createUser(@Valid UserFormDTO userFormDTO) throws UserException {
        if (userRepository.existsByEmail(userFormDTO.email())) {
            throw new UserAlreadyExistsException("Email already registered");
        }
        if (userRepository.existsByCpf(userFormDTO.cpf())) {
            throw new UserAlreadyExistsException("CPF already registered");
        }
        var password = securityConfigurations.passwordEncoder().encode(userFormDTO.password());
        var user = new User(userFormDTO, password);
        userRepository.saveAndFlush(user);
        return user;
    }

    public UserResponseDTO getUserByCPF(String cpf) throws UserException {
        var user = userRepository.findByCpf(cpf);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return new UserResponseDTO(user);
    }
}

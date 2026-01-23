package br.com.baba.eventHub.api.controller;

import br.com.baba.eventHub.core.dto.UserFormDTO;
import br.com.baba.eventHub.core.exceptions.UserException;
import br.com.baba.eventHub.core.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity createUser(@Valid @RequestBody UserFormDTO userFormDTO) throws UserException {
        userService.createUser(userFormDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{cpf}")
    @SecurityRequirement(name = "bearer-key")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity getUser(@PathVariable String cpf) throws UserException {
        var userDTO = userService.getUserByCPF(cpf);
        return ResponseEntity.ok(userDTO);
    }
}

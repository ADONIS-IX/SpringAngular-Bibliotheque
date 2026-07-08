package com.projet.bibliotheque.controller;

import com.projet.bibliotheque.dto.CreateUserRequest;
import com.projet.bibliotheque.dto.UpdateUserRequest;
import com.projet.bibliotheque.dto.UtilisateurDto;
import com.projet.bibliotheque.model.Utilisateur;
import com.projet.bibliotheque.service.DtoMapper;
import com.projet.bibliotheque.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final DtoMapper dtoMapper;

    public AdminController(UserService userService, DtoMapper dtoMapper) {
        this.userService = userService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        Utilisateur user = userService.createUser(request.nom(), request.email(), request.password(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toUtilisateurDto(user));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.List<UtilisateurDto> listUsers() {
        return userService.lister().stream().map(dtoMapper::toUtilisateurDto).toList();
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurDto> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        Utilisateur u = userService.updateUser(id, req);
        return ResponseEntity.ok(dtoMapper.toUtilisateurDto(u));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

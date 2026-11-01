package org.example.university.controller;

import org.example.university.entity.Role;
import org.example.university.entity.User;
import org.example.university.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Get all admins
    @GetMapping
    public ResponseEntity<List<User>> getAllAdmins() {
        return ResponseEntity.ok(userRepository.findByRole(Role.ROLE_ADMIN));
    }

    // 2. Get admin by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getAdminById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getRole() == Role.ROLE_ADMIN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found with id: " + id));
        return ResponseEntity.ok(user);
    }

    // 3. Create a new admin
    @PostMapping
    public ResponseEntity<User> createAdmin(@RequestBody User adminRequest) {
        if (!StringUtils.hasText(adminRequest.getUsername()) || !StringUtils.hasText(adminRequest.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
        if (userRepository.existsByUsername(adminRequest.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }
        if (userRepository.existsByEmail(adminRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User newAdmin = new User();
        newAdmin.setUsername(adminRequest.getUsername());
        newAdmin.setEmail(adminRequest.getEmail());
        newAdmin.setPassword(passwordEncoder.encode(adminRequest.getPassword()));
        newAdmin.setRole(Role.ROLE_ADMIN);

        User saved = userRepository.save(newAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 4. Update admin
    @PutMapping("/{id}")
    public ResponseEntity<User> updateAdmin(@PathVariable Long id, @RequestBody User adminRequest) {
        User existing = userRepository.findById(id)
                .filter(u -> u.getRole() == Role.ROLE_ADMIN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found with id: " + id));

        if (StringUtils.hasText(adminRequest.getUsername())) {
            existing.setUsername(adminRequest.getUsername());
        }
        if (StringUtils.hasText(adminRequest.getEmail())) {
            existing.setEmail(adminRequest.getEmail());
        }
        if (StringUtils.hasText(adminRequest.getPassword())) {
            existing.setPassword(passwordEncoder.encode(adminRequest.getPassword()));
        }

        User updated = userRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    // 5. Delete admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        User existing = userRepository.findById(id)
                .filter(u -> u.getRole() == Role.ROLE_ADMIN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found with id: " + id));

        userRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }
}

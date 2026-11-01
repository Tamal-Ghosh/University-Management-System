package org.example.university.controller;

import org.example.university.entity.Role;
import org.example.university.entity.Teacher;
import org.example.university.entity.User;
import org.example.university.repository.TeacherRepository;
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
@RequestMapping("/api/teachers")
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class TeacherController {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherController(TeacherRepository teacherRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Get all teachers
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        return ResponseEntity.ok(teacherRepository.findAll());
    }

    // 2. Get teacher by ID
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found with id: " + id));
        return ResponseEntity.ok(teacher);
    }

    // 3. Create a new teacher
    @PostMapping
    public ResponseEntity<Teacher> createTeacher(@RequestBody Teacher teacherRequest) {
        if (teacherRequest.getUser() == null ||
                !StringUtils.hasText(teacherRequest.getUser().getUsername()) ||
                !StringUtils.hasText(teacherRequest.getUser().getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User details (username, password) are required");
        }

        if (userRepository.existsByUsername(teacherRequest.getUser().getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmail(teacherRequest.getUser().getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = teacherRequest.getUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_TEACHER);

        teacherRequest.setUser(user);
        Teacher savedTeacher = teacherRepository.save(teacherRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTeacher);
    }

    // 4. Update teacher
    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @RequestBody Teacher teacherRequest) {
        Teacher existingTeacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found with id: " + id));

        if (StringUtils.hasText(teacherRequest.getDepartment())) {
            existingTeacher.setDepartment(teacherRequest.getDepartment());
        }
        if (StringUtils.hasText(teacherRequest.getDesignation())) {
            existingTeacher.setDesignation(teacherRequest.getDesignation());
        }

        if (teacherRequest.getUser() != null) {
            User existingUser = existingTeacher.getUser();
            if (StringUtils.hasText(teacherRequest.getUser().getEmail())) {
                existingUser.setEmail(teacherRequest.getUser().getEmail());
            }
            if (StringUtils.hasText(teacherRequest.getUser().getPassword())) {
                existingUser.setPassword(passwordEncoder.encode(teacherRequest.getUser().getPassword()));
            }
        }

        Teacher updated = teacherRepository.save(existingTeacher);
        return ResponseEntity.ok(updated);
    }

    // 5. Delete teacher
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        Teacher existingTeacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found with id: " + id));

        teacherRepository.delete(existingTeacher);
        return ResponseEntity.noContent().build();
    }
}

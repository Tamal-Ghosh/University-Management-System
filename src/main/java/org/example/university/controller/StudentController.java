package org.example.university.controller;

import org.example.university.entity.Role;
import org.example.university.entity.Student;
import org.example.university.entity.User;
import org.example.university.repository.StudentRepository;
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
@RequestMapping("/api/students")
@PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
public class StudentController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentController(StudentRepository studentRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Get all students
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    // 2. Get student by ID
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id));
        return ResponseEntity.ok(student);
    }

    // 3. Create a new student
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student studentRequest) {
        if (studentRequest.getUser() == null ||
                !StringUtils.hasText(studentRequest.getUser().getUsername()) ||
                !StringUtils.hasText(studentRequest.getUser().getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User credentials (username, password) are required");
        }

        if (!StringUtils.hasText(studentRequest.getStudentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student ID (e.g. roll/matric number) is required");
        }

        if (studentRepository.existsByStudentId(studentRequest.getStudentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student ID already exists");
        }

        if (userRepository.existsByUsername(studentRequest.getUser().getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmail(studentRequest.getUser().getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = studentRequest.getUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_STUDENT);

        studentRequest.setUser(user);
        Student savedStudent = studentRepository.save(studentRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
    }

    // 4. Update student
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentRequest) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id));

        if (StringUtils.hasText(studentRequest.getDepartment())) {
            existingStudent.setDepartment(studentRequest.getDepartment());
        }
        if (StringUtils.hasText(studentRequest.getSemester())) {
            existingStudent.setSemester(studentRequest.getSemester());
        }
        if (StringUtils.hasText(studentRequest.getStudentId())) {
            existingStudent.setStudentId(studentRequest.getStudentId());
        }

        if (studentRequest.getUser() != null) {
            User existingUser = existingStudent.getUser();
            if (StringUtils.hasText(studentRequest.getUser().getEmail())) {
                existingUser.setEmail(studentRequest.getUser().getEmail());
            }
            if (StringUtils.hasText(studentRequest.getUser().getPassword())) {
                existingUser.setPassword(passwordEncoder.encode(studentRequest.getUser().getPassword()));
            }
        }

        Student updated = studentRepository.save(existingStudent);
        return ResponseEntity.ok(updated);
    }

    // 5. Delete student
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id));

        studentRepository.delete(existingStudent);
        return ResponseEntity.noContent().build();
    }
}

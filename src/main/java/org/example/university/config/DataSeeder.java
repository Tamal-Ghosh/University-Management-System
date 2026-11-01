package org.example.university.config;

import org.example.university.entity.Role;
import org.example.university.entity.Student;
import org.example.university.entity.Teacher;
import org.example.university.entity.User;
import org.example.university.repository.StudentRepository;
import org.example.university.repository.TeacherRepository;
import org.example.university.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      TeacherRepository teacherRepository,
                      StudentRepository studentRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Root Admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User(
                    "admin",
                    "admin@university.com",
                    passwordEncoder.encode("admin123"),
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);
            System.out.println(">>> [DataSeeder] Default Admin created (admin / admin123)");
        }

        // 2. Sample Teachers
        if (teacherRepository.count() == 0) {
            User u1 = new User("prof_rafiq", "rafiq@univ.edu", passwordEncoder.encode("password123"), Role.ROLE_TEACHER);
            Teacher t1 = new Teacher(u1, "Computer Science & Engineering", "Professor & Head");
            teacherRepository.save(t1);

            User u2 = new User("nusrat_jahan", "nusrat@univ.edu", passwordEncoder.encode("password123"), Role.ROLE_TEACHER);
            Teacher t2 = new Teacher(u2, "Electrical & Electronic Engineering", "Assistant Professor");
            teacherRepository.save(t2);

            User u3 = new User("tanvir_ahmed", "tanvir@univ.edu", passwordEncoder.encode("password123"), Role.ROLE_TEACHER);
            Teacher t3 = new Teacher(u3, "Software Engineering", "Associate Professor");
            teacherRepository.save(t3);
            System.out.println(">>> [DataSeeder] Initial faculty members seeded.");
        }

        // 3. Sample Students
        if (studentRepository.count() == 0) {
            User sUser1 = new User("sadia_rahman", "sadia@univ.edu", passwordEncoder.encode("password123"), Role.ROLE_STUDENT);
            Student s1 = new Student(sUser1, "CSE-2022-045", "Computer Science & Engineering", "7th Semester");
            studentRepository.save(s1);

            User sUser2 = new User("abrar_fahim", "abrar@univ.edu", passwordEncoder.encode("password123"), Role.ROLE_STUDENT);
            Student s2 = new Student(sUser2, "SWE-2023-012", "Software Engineering", "5th Semester");
            studentRepository.save(s2);

            User sUser3 = new User("mehedi_hasan", "mehedi@univ.edu", passwordEncoder.encode("password123"), Role.ROLE_STUDENT);
            Student s3 = new Student(sUser3, "EEE-2024-089", "Electrical & Electronic Engineering", "3rd Semester");
            studentRepository.save(s3);
            System.out.println(">>> [DataSeeder] Initial students seeded.");
        }
    }
}

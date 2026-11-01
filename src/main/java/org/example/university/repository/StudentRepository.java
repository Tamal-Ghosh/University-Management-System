package org.example.university.repository;

import org.example.university.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByUserUsername(String username);
    Optional<Student> findByStudentId(String studentId);
    Boolean existsByStudentId(String studentId);
    Boolean existsByUserUsername(String username);
    Boolean existsByUserEmail(String email);
}

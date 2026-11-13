package org.example.university.controller;

import org.example.university.entity.*;
import org.example.university.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePermissionsTest {
    @Mock StudentRepository students;
    @Mock TeacherRepository teachers;
    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    StudentController studentController;
    TeacherController teacherController;

    @BeforeEach
    void setUp() {
        studentController = new StudentController(students, users, encoder);
        teacherController = new TeacherController(teachers, users, encoder);
    }

    @Test
    void studentCanUpdateOwnProfile() {
        Student existing = student("owner");
        when(students.findById(1L)).thenReturn(Optional.of(existing));
        when(students.save(existing)).thenReturn(existing);
        Student request = new Student();
        request.setDepartment("Updated CSE");
        var response = studentController.updateStudent(1L, request, auth("owner", "STUDENT"));
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getDepartment()).isEqualTo("EEE");
        verify(students).save(existing);
    }

    @Test
    void studentCannotUpdateAnotherStudentsProfileOrPassword() {
        Student existing = student("other");
        when(students.findById(1L)).thenReturn(Optional.of(existing));
        Student request = student("attacker");
        request.setDepartment("Unwanted change");
        assertThatThrownBy(() -> studentController.updateStudent(1L, request, auth("attacker", "STUDENT")))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(existing.getDepartment()).isEqualTo("CSE");
        assertThat(existing.getUser().getPassword()).isEqualTo("original-hash");
        verify(students, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void teacherCanUpdateAnyStudent() {
        Student existing = student("student");
        when(students.findById(1L)).thenReturn(Optional.of(existing));
        when(students.save(existing)).thenReturn(existing);
        Student request = new Student();
        request.setSemester("8th");
        studentController.updateStudent(1L, request, auth("teacher", "TEACHER"));
        assertThat(existing.getSemester()).isEqualTo("8th");
        verify(students).save(existing);
    }

    @Test
    void teacherCanUpdateOwnProfile() {
        Teacher existing = teacher("owner");
        when(teachers.findById(1L)).thenReturn(Optional.of(existing));
        when(teachers.save(existing)).thenReturn(existing);
        Teacher request = new Teacher();
        request.setDesignation("Professor");
        teacherController.updateTeacher(1L, request, auth("owner", "TEACHER"));
        assertThat(existing.getDesignation()).isEqualTo("Professor");
        verify(teachers).save(existing);
    }

    @Test
    void teacherCannotUpdateAnotherTeachersProfile() {
        Teacher existing = teacher("other");
        when(teachers.findById(1L)).thenReturn(Optional.of(existing));
        Teacher request = new Teacher();
        request.setDepartment("Unwanted change");
        assertThatThrownBy(() -> teacherController.updateTeacher(1L, request, auth("attacker", "TEACHER")))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(existing.getDepartment()).isEqualTo("CSE");
        verify(teachers, never()).save(any());
    }

    @Test
    void adminCanUpdateAnotherUsersProfile() {
        Teacher existing = teacher("teacher");
        when(teachers.findById(1L)).thenReturn(Optional.of(existing));
        when(teachers.save(existing)).thenReturn(existing);
        Teacher request = new Teacher();
        request.setDepartment("EEE");
        teacherController.updateTeacher(1L, request, auth("admin", "ADMIN"));
        assertThat(existing.getDepartment()).isEqualTo("EEE");
        verify(teachers).save(existing);
    }

    private Authentication auth(String username, String role) {
        return new UsernamePasswordAuthenticationToken(username, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private Student student(String username) {
        return new Student(new User(username, username + "@test.edu", "original-hash", Role.ROLE_STUDENT),
                "ROLL-1", "CSE", "1st");
    }

    private Teacher teacher(String username) {
        return new Teacher(new User(username, username + "@test.edu", "original-hash", Role.ROLE_TEACHER),
                "CSE", "Lecturer");
    }
}

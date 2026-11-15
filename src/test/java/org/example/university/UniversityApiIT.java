package org.example.university;

import jakarta.persistence.EntityManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.university.entity.*;
import org.example.university.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UniversityApiIT {
    @Autowired EntityManager entityManager;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired StudentRepository students;
    @Autowired TeacherRepository teachers;
    @Autowired PasswordEncoder encoder;
    Student student;
    Student otherStudent;
    Teacher teacher;
    Teacher otherTeacher;

    @BeforeEach
    void createFixtures() {
        users.save(user("it_admin", Role.ROLE_ADMIN));
        student = students.save(new Student(user("it_student", Role.ROLE_STUDENT), "IT-001", "CSE", "1st"));
        otherStudent = students.save(new Student(user("it_other_student", Role.ROLE_STUDENT), "IT-002", "EEE", "2nd"));
        teacher = teachers.save(new Teacher(user("it_teacher", Role.ROLE_TEACHER), "CSE", "Lecturer"));
        otherTeacher = teachers.save(new Teacher(user("it_other_teacher", Role.ROLE_TEACHER), "EEE", "Lecturer"));
    }

    @Test
    void realLoginAuthenticatesProtectedRequestsAndRejectsInvalidCredentials() throws Exception {
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", "it_student", "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());
        String token = login("it_student");
        mvc.perform(get("/api/auth/me").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("it_student"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/students").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(get("/api/admins").header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateReadUpdateAndDeleteAStudentWithPersistedPasswordHash() throws Exception {
        String token = login("it_admin");
        String body = json.writeValueAsString(Map.of("studentId", "IT-NEW", "department", "CSE", "semester", "1st",
                "user", Map.of("username", "it_new", "email", "it_new@test.edu", "password", "new-password")));
        String response = mvc.perform(post("/api/students").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(response).get("id").asLong();
        reloadPersistenceContext();
        assertThat(encoder.matches("new-password", users.findByUsername("it_new").orElseThrow().getPassword())).isTrue();
        mvc.perform(get("/api/students/{id}", id).header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.studentId").value("IT-NEW"));
        mvc.perform(put("/api/students/{id}", id).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"semester\":\"2nd\"}"))
                .andExpect(status().isOk());
        reloadPersistenceContext();
        assertThat(students.findById(id).orElseThrow().getSemester()).isEqualTo("2nd");
        mvc.perform(delete("/api/students/{id}", id).header("Authorization", token)).andExpect(status().isNoContent());
        reloadPersistenceContext();
        assertThat(students.findById(id)).isEmpty();
        assertThat(users.findByUsername("it_new")).isEmpty();
        mvc.perform(get("/api/students/{id}", id).header("Authorization", token)).andExpect(status().isNotFound());
    }

    @Test
    void editPermissionsEnforceStudentOwnershipAndTeacherAndAdminPrivileges() throws Exception {
        String studentToken = login("it_student");
        String teacherToken = login("it_teacher");
        String adminToken = login("it_admin");
        update("students", student.getId(), studentToken, "Own update", 200);
        String originalPassword = otherStudent.getUser().getPassword();
        mvc.perform(put("/api/students/{id}", otherStudent.getId()).header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"department\":\"Forbidden\",\"user\":{\"password\":\"hacked\"}}"))
                .andExpect(status().isForbidden());
        reloadPersistenceContext();
        assertThat(students.findById(otherStudent.getId()).orElseThrow().getDepartment()).isEqualTo("EEE");
        assertThat(users.findByUsername("it_other_student").orElseThrow().getPassword()).isEqualTo(originalPassword);
        update("teachers", teacher.getId(), studentToken, "Forbidden", 403);
        update("students", otherStudent.getId(), teacherToken, "Teacher update", 200);
        update("teachers", teacher.getId(), teacherToken, "Own update", 200);
        update("teachers", otherTeacher.getId(), teacherToken, "Forbidden", 403);
        reloadPersistenceContext();
        assertThat(teachers.findById(otherTeacher.getId()).orElseThrow().getDepartment()).isEqualTo("EEE");
        update("students", student.getId(), adminToken, "Admin update", 200);
        update("teachers", otherTeacher.getId(), adminToken, "Admin update", 200);
    }

    private void update(String resource, Long id, String token, String department, int expected) throws Exception {
        mvc.perform(put("/api/" + resource + "/{id}", id).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("department", department))))
                .andExpect(status().is(expected));
        reloadPersistenceContext();
        if (expected == 200) {
            mvc.perform(get("/api/" + resource + "/{id}", id).header("Authorization", token))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.department").value(department));
        }
    }

    private void reloadPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }

    private String login(String username) throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", username, "password", "test-password"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        JsonNode result = json.readTree(response);
        return "Bearer " + result.get("token").asText();
    }

    private User user(String username, Role role) {
        return new User(username, username + "@test.edu", encoder.encode("test-password"), role);
    }
}

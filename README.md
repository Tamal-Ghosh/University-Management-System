# 🎓 University Management System (Spring Boot + PostgreSQL + Docker)

A robust, enterprise-grade **University Management System** web application developed using **Java Spring Boot 3**, **Spring Security 6**, **PostgreSQL**, and **Docker**. It implements Role-Based Access Control (RBAC) across three distinct user roles: **Admin**, **Teacher**, and **Student**.

---

## 🌟 Key Features

* **RESTful API Architecture:** Full CRUD operations (Create, Read, Update, Delete) for Admin, Teacher, and Student resources with standard HTTP status codes (`200`, `201`, `204`, `400`, `401`, `403`, `404`).
* **Spring Security & JWT Authentication:** 
  * Stateless token-based security via JJWT.
  * BCrypt password hashing.
  * Role-Based Access Control (RBAC) enforced via `SecurityFilterChain` and `@PreAuthorize`.
* **RBAC Matrix:**
  * 👑 **Admin (`ROLE_ADMIN`):** Unrestricted access to manage Administrators, Teachers, and Students.
  * 👨‍🏫 **Teacher (`ROLE_TEACHER`):** Authorized to manage only Teacher resources.
  * 🎓 **Student (`ROLE_STUDENT`):** Authorized to manage only Student resources.
* **Dockerized Infrastructure:** 
  * Multi-stage `Dockerfile` (Maven 3.9 builder + lightweight Eclipse Temurin 21 JRE Alpine runtime).
  * `compose.yaml` orchestrating PostgreSQL 16 and Spring Boot with automated healthchecks and persistent volumes.
* **Modern Web Dashboard:** Clean, responsive, interactive single-page dashboard (`index.html`) with real-time search, dynamic role tabs, and modal-based CRUD forms.

---

## 🏗️ Tech Stack

* **Backend:** Java 21, Spring Boot 3.3.5
* **Security:** Spring Security 6, JJWT 0.12.6, BCrypt
* **ORM & Database:** Spring Data JPA, Hibernate, PostgreSQL 16 (H2 available for local in-memory profile)
* **Frontend:** HTML5, Tailwind CSS, JavaScript (Vanilla fetch API)
* **Containerization:** Docker & Docker Compose

---

## 🚀 Quick Start with Docker

Ensure Docker Desktop is running, then run:

```bash
docker compose up --build
```

Access the application at:
👉 **http://localhost:8081**

---

## 💻 Local Development (Without Docker)

You can run the application locally using the in-memory H2 profile:

```bash
# Package the project
./mvnw clean package -DskipTests

# Run with local profile on port 8081
java -jar target/University-0.0.1-SNAPSHOT.jar --server.port=8081 --spring.profiles.active=local
```

Access the dashboard at:
👉 **http://localhost:8081**

---

## 🔑 Default Credentials (Seeded)

The database automatically seeds the following initial accounts on startup:

| Role | Username | Password | Email | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | `admin@university.com` | Root Administrator |
| **Admin** | `registrar` | `password123` | `registrar@univ.edu` | Academic Registrar |
| **Teacher** | `prof_rafiq` | `password123` | `rafiq@univ.edu` | Professor & Head (CSE) |
| **Teacher** | `nusrat_jahan` | `password123` | `nusrat@univ.edu` | Assistant Professor (EEE) |
| **Teacher** | `tanvir_ahmed` | `password123` | `tanvir@univ.edu` | Associate Professor (SWE) |
| **Student** | `sadia_rahman` | `password123` | `sadia@univ.edu` | CSE-2022-045 (7th Sem) |
| **Student** | `abrar_fahim` | `password123` | `abrar@univ.edu` | SWE-2023-012 (5th Sem) |
| **Student** | `mehedi_hasan` | `password123` | `mehedi@univ.edu` | EEE-2024-089 (3rd Sem) |

---

## 📡 REST API Documentation

### 1. Authentication Endpoints
* `POST /api/auth/login` — Authenticate and retrieve JWT Bearer token
* `GET /api/auth/me` — Retrieve current authenticated user profile
* `POST /api/auth/logout` — Invalidate user session

### 2. Teacher Resource (`/api/teachers`) — *Accessible by ADMIN, TEACHER*
* `GET /api/teachers` — List all teachers
* `GET /api/teachers/{id}` — Get teacher by ID
* `POST /api/teachers` — Register new teacher
* `PUT /api/teachers/{id}` — Update teacher profile
* `DELETE /api/teachers/{id}` — Remove teacher

### 3. Student Resource (`/api/students`) — *Accessible by ADMIN, STUDENT*
* `GET /api/students` — List all students
* `GET /api/students/{id}` — Get student by ID
* `POST /api/students` — Register new student
* `PUT /api/students/{id}` — Update student profile
* `DELETE /api/students/{id}` — Remove student

### 4. Admin Resource (`/api/admins`) — *Accessible by ADMIN only*
* `GET /api/admins` — List all administrators
* `GET /api/admins/{id}` — Get administrator by ID
* `POST /api/admins` — Register new administrator
* `PUT /api/admins/{id}` — Update administrator
* `DELETE /api/admins/{id}` — Remove administrator

---

## 📜 License
This project is open-source and available under the [MIT License](LICENSE).

# 🎓 Learning Management System (LMS)

A complete Learning Management System built with **Spring Boot** and **MySQL**, supporting **role-based access** for Admins, Instructors, and Students. This project simulates a real-world LMS with user registration, course creation, enrollment, assignments, quizzes, and attendance via OTP.

## 🔧 Technologies Used

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- Postman (for API testing)
- Maven

---

## 👥 User Roles & Features

### 🛠 Admin
- Create users (Admins, Instructors, Students)
- Manage courses (create/delete/update)
- View enrolled students in courses

### 👨‍🏫 Instructor
- Create and manage courses
- Upload course media (videos, PDFs, etc.)
- Add lessons, assignments, and quizzes
- Grade assignments and quizzes
- View and remove students from courses
- Generate OTP for lesson attendance

### 👩‍🎓 Student
- View and enroll in available courses
- Access course materials
- Submit assignments and take quizzes
- View grades and feedback
- Attend lessons via OTP

---

## 🛡️ Authentication & Security

- Role-based authorization (Admin, Instructor, Student)
- Secure endpoints with Spring Security

---

## 📁 Project Structure

```bash
src/
├── controller/         # REST controllers
├── model/              # Entity classes
├── repository/         # JPA repositories
├── service/            # Business logic
└── config/             # Security configuration

# AssessIQ – Online MCQ Assessment Platform

AssessIQ is a full-stack online MCQ assessment platform built with Java, Spring Boot, React, TypeScript and MySQL.

The platform provides separate experiences for teachers and students. Teachers can create and manage assessments, questions and options, publish tests and review student attempts. Students can browse available assessments, take tests, submit answers and view their results and attempt history.

This project was built as a practical portfolio project to demonstrate full-stack development, REST API design, authentication, database design and real-world assessment workflows.

---

## Features

### Teacher

- Secure login with JWT authentication
- Teacher dashboard
- Create assessments
- Update and delete assessments
- Add, edit and delete questions
- Add and manage MCQ options
- Mark correct answers
- Publish assessments
- Close assessments
- View student attempts
- View student scores and percentages
- Review individual student answers
- Compare selected answers with correct answers
- Assessment ownership validation

### Student

- Student registration and login
- JWT-based authentication
- Student dashboard
- Browse available assessments
- View assessment details
- Start an assessment
- Answer MCQ questions
- Save/update answers
- Assessment timer
- Submit assessment
- Server-side score calculation
- View result
- View previous assessment history
- Correct answers remain hidden while an assessment is in progress

---

## Application Demo

> Demo video coming soon.

A short walkthrough video will be added here showing the complete application flow.

---

## Application Screens

The application currently includes:

- Login screen
- Teacher dashboard
- Student dashboard
- Assessment browsing
- Assessment history
- Assessment attempt flow
- Result and performance views
- Teacher assessment management
- Student attempt review

The UI uses a dark, modern assessment-SaaS design with responsive layouts, cards, clear status indicators and subtle interactions.

---

## Technology Stack

### Backend

- Java 17
- Spring Boot 3.3.6
- Spring Security 6
- Spring Data JPA
- Hibernate 6
- REST APIs
- JWT
- JJWT 0.12.6
- BCrypt
- Maven
- Lombok

### Frontend

- React
- TypeScript
- Vite
- React Router
- Axios
- CSS

### Database

- MySQL 8

### Deployment

- Render – Spring Boot backend
- Aiven – MySQL database
- Netlify – React frontend

---

## Architecture

```text
                    ┌──────────────────────┐
                    │      React UI        │
                    │   Vite + TypeScript  │
                    └──────────┬───────────┘
                               │
                               │ REST API / JSON
                               ▼
                    ┌──────────────────────┐
                    │   Spring Boot API    │
                    │                      │
                    │ Spring Security      │
                    │ JWT Authentication   │
                    │ Service Layer        │
                    │ REST Controllers     │
                    └──────────┬───────────┘
                               │
                               │ JPA / Hibernate
                               ▼
                    ┌──────────────────────┐
                    │       MySQL          │
                    │                      │
                    │ Users                │
                    │ Assessments          │
                    │ Questions            │
                    │ Options              │
                    │ Attempts              │
                    │ Student Answers      │
                    └──────────────────────┘
```

---

## User Roles

### TEACHER

Teachers manage assessments and review student performance.

### STUDENT

Students can view published assessments, take tests and review their own results.

The current version uses `TEACHER` as the administrative role. A separate `ADMIN` role can be added in a future version.

---

## Assessment Lifecycle

```text
DRAFT
  │
  ▼
PUBLISHED
  │
  ▼
CLOSED
```

### Draft

Teachers can create and modify the assessment, questions and options.

### Published

Students can view and attempt the assessment.

Question and option management is locked after publishing.

### Closed

The assessment is no longer available for new attempts.

---

## Assessment Flow

### Teacher Flow

```text
Login
  ↓
Teacher Dashboard
  ↓
Create Assessment
  ↓
Add Questions
  ↓
Add MCQ Options
  ↓
Select Correct Answers
  ↓
Publish
  ↓
Students Attempt Test
  ↓
View Attempts
  ↓
Review Student Answers
```

### Student Flow

```text
Register / Login
       ↓
Student Dashboard
       ↓
Browse Assessments
       ↓
Assessment Details
       ↓
Start Assessment
       ↓
Answer Questions
       ↓
Submit
       ↓
Server-Side Evaluation
       ↓
View Result
       ↓
View History
```

---

## Security

The backend uses Spring Security with stateless JWT authentication.

Security features include:

- JWT authentication
- BCrypt password hashing
- Role-based authorization
- Protected teacher and student endpoints
- Teacher ownership validation
- Student attempt ownership validation
- Correct answers hidden during active tests
- Server-side scoring
- Duplicate submission prevention
- Assessment duration enforcement

---

## Server-Side Scoring

The backend is the source of truth for assessment results.

The frontend does not calculate the authoritative score.

When a student submits an assessment:

```text
Student Answers
      |
      v
Spring Boot Backend
      |
      v
Compare Selected Options
      |
      v
Calculate Marks
      |
      v
Calculate Percentage
      |
      v
Store Attempt Result
```

Example:

```text
Total Marks : 5
Score       : 4
Percentage  : 80%
Status      : SUBMITTED
```

---

## Database Design

The application uses six main tables:

```text
users
   |
   +---- assessments
   |        |
   |        +---- questions
   |                |
   |                +---- options
   |
   +---- assessment_attempts
              |
              +---- student_answers
```

### Tables

| Table | Purpose |
|---|---|
| `users` | Stores teachers and students |
| `assessments` | Stores assessment information |
| `questions` | Stores assessment questions |
| `options` | Stores MCQ options and correct answers |
| `assessment_attempts` | Stores student attempts and results |
| `student_answers` | Stores answers selected by students |

---

## API Overview

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

### Teacher APIs

```http
POST   /api/teacher/assessments
GET    /api/teacher/assessments
GET    /api/teacher/assessments/{id}
PUT    /api/teacher/assessments/{id}
DELETE /api/teacher/assessments/{id}

POST   /api/teacher/assessments/{assessmentId}/questions
PUT    /api/teacher/questions/{questionId}
DELETE /api/teacher/questions/{questionId}

POST   /api/teacher/questions/{questionId}/options
PUT    /api/teacher/options/{optionId}
DELETE /api/teacher/options/{optionId}

POST   /api/teacher/assessments/{id}/publish
POST   /api/teacher/assessments/{id}/close

GET    /api/teacher/assessments/{id}/attempts
GET    /api/teacher/attempts/{attemptId}/answers
```

### Student APIs

```http
GET    /api/student/assessments
GET    /api/student/assessments/{id}

POST   /api/student/assessments/{assessmentId}/start

GET    /api/student/attempts/{attemptId}

PUT    /api/student/attempts/{attemptId}/answers/{questionId}

POST   /api/student/attempts/{attemptId}/submit

GET    /api/student/attempts
GET    /api/student/attempts/{attemptId}/result
```

---

## Demo Data

The demo environment contains:

### Teachers

```text
teacher1
admin_teacher
```

### Students

```text
student_alex
Ace
Rachel
Charles
```

### Assessments

```text
Java & OOP Fundamentals
SQL & Database Fundamentals
Spring Boot & REST APIs
React Fundamentals
```

The demo database contains a small number of completed attempts so the teacher dashboard can demonstrate student performance and detailed answer review.

Students can also create fresh attempts themselves to demonstrate the complete assessment workflow.

---

## Local Setup

### Prerequisites

- Java 17
- Maven
- Node.js
- MySQL 8
- Git

### Backend

Clone the repository:

```bash
git clone <your-backend-repository-url>
```

Go to the backend project:

```bash
cd <backend-project>
```

Configure the database in `application.properties` or environment variables:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/assessment_system
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

Configure JWT:

```properties
app.jwt.secret=YOUR_SECRET
```

Run the backend:

```bash
./mvnw spring-boot:run
```

Windows:

```cmd
mvnw.cmd spring-boot:run
```

Backend:

```text
http://localhost:8080
```

### Frontend

Go to the frontend project:

```bash
cd assessment-frontend
```

Install dependencies:

```bash
npm install
```

Create a `.env` file:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Start the frontend:

```bash
npm run dev
```

Frontend:

```text
http://localhost:5173
```

---

## Deployment

The project can be deployed using free-tier cloud services:

```text
React Frontend
      |
      v
   Netlify
      |
   REST API
      |
      v
Spring Boot Backend
      |
      v
    Render
      |
      v
    Aiven
      |
      v
    MySQL
```

Production credentials and secrets should be stored as environment variables and should never be committed to GitHub.

---

## Future Improvements

Planned improvements include:

- AI-powered MCQ generation
- PDF and Word document upload
- AI extraction of questions from uploaded documents
- Teacher review of AI-generated questions
- Question difficulty levels
- Question randomization
- Option randomization
- Negative marking
- Question bank
- Assessment scheduling
- Advanced analytics
- Leaderboards
- Email notifications
- Separate ADMIN role

### Planned AI Workflow

```text
Teacher
   |
   v
Upload PDF / Word Document
   |
   v
AI Processing
   |
   v
Generate MCQs
   |
   v
Teacher Reviews Questions
   |
   v
Publish Assessment
   |
   v
Students Attempt
```

---

## Project Structure

### Backend

```text
src/
├── main/
│   ├── java/
│   └── resources/
│       └── application.properties
├── test/
└── pom.xml
```

### Frontend

```text
src/
├── api/
├── components/
├── context/
├── pages/
├── routes/
├── types/
├── utils/
├── App.tsx
└── main.tsx
```

---

## Author

**Omkar Patil**

Full Stack Java Developer

Primary technologies:

`Java` · `Spring Boot` · `Spring Security` · `REST APIs` · `Hibernate` · `JPA` · `React` · `TypeScript` · `MySQL` · `JWT`

---

## License

This project is created for educational, portfolio and demonstration purposes.

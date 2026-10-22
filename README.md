# BookLoop — University Student Textbook Marketplace

BookLoop is a C2C (consumer-to-consumer) web application that enables university students to buy and sell used textbooks directly with each other. Built with **Java 17**, **Spring Boot 3.3**, **Thymeleaf**, **Bootstrap 5**, and **Microsoft SQL Server**.

---

## Table of Contents

1. [Features (Phase 3)](#features-phase-3)
2. [Technology Stack](#technology-stack)
3. [Prerequisites](#prerequisites)
4. [Database Setup](#database-setup)
5. [Configuration](#configuration)
6. [Running the Application](#running-the-application)
7. [Running Tests](#running-tests)
8. [Main Routes](#main-routes)
9. [Project Structure](#project-structure)
10. [Known Limitations](#known-limitations)
11. [Future Phases](#future-phases)

---

## Features (Phase 3)

- **Student Registration & Login** — BCrypt-hashed passwords, session-based authentication, CSRF protection, duplicate email validation, password confirmation.
- **Homepage** — Hero section, stats bar, recently listed books grid loaded from the database.
- **Browse & Search Books** — Filter by keyword, category, campus, condition, price range. Full pagination.
- **Book Detail Page** — Full listing details with seller info, condition badges, defect notes.
- **Profile Management** — View and edit personal profile (only safe fields: name, university, campus, phone, avatar URL).
- **My Listings** — View all your listings with status, responsive table/card layout.
- **Create, Edit, Delete Listings** — Full CRUD with server-side validation, ownership enforcement, delete confirmation, Post/Redirect/Get pattern.
- **Public Seller Profiles** — View a seller's public info and available listings.
- **Responsive Design** — Works on desktop, tablet, and mobile.
- **Error Handling** — Graceful 400, 403, 404, and generic error pages.

---

## Technology Stack

| Layer         | Technology                                |
|---------------|-------------------------------------------|
| Language      | Java 17                                   |
| Framework     | Spring Boot 3.3.5                         |
| Build Tool    | Maven (wrapper included)                  |
| Template      | Thymeleaf + Thymeleaf Security Extras     |
| CSS           | Bootstrap 5.3 (CDN) + Custom CSS          |
| Icons         | Font Awesome 6 (CDN)                      |
| Database      | Microsoft SQL Server                      |
| ORM           | Spring Data JPA / Hibernate               |
| Security      | Spring Security 6 (BCrypt, sessions, CSRF)|
| Validation    | Jakarta Bean Validation                   |
| Testing       | JUnit 5, Spring Test, MockMvc, H2         |

---

## Prerequisites

1. **Java 17+** — Download from [Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or [Adoptium](https://adoptium.net/).
2. **Maven 3.8+** — The project includes a Maven wrapper (`mvnw.cmd`), so a separate Maven install is optional.
3. **Microsoft SQL Server** — Any edition (Express, Developer, Standard). Download from [Microsoft](https://www.microsoft.com/en-us/sql-server/sql-server-downloads).
4. **SQL Server Management Studio (SSMS)** *(optional but recommended)* — For running schema scripts and inspecting data.

---

## Database Setup

### Step 1 — Create the database

Open SSMS or `sqlcmd` and run:

```sql
CREATE DATABASE BookLoopDB;
```

Or run the full schema script provided at `sql/schema.sql`:

```
sqlcmd -S localhost -i sql/schema.sql
```

> **Note:** If your SQL Server uses a named instance (e.g., `localhost\SQLEXPRESS`), use:
> ```
> sqlcmd -S localhost\SQLEXPRESS -i sql/schema.sql
> ```

### Step 2 — Verify connection

Ensure SQL Server:
- Is running and accepting TCP connections.
- Has TCP/IP enabled in SQL Server Configuration Manager.
- Has a login (e.g., `sa`) with a known password.
- The port is known (default is **1433**, but named instances may use a different port).

To find your instance's port:
1. Open **SQL Server Configuration Manager**.
2. Navigate to **SQL Server Network Configuration** → **Protocols for [Instance]** → **TCP/IP** → **IP Addresses**.
3. Look at the **IPAll** section for `TCP Port`.

---

## Configuration

The application reads database credentials from **environment variables**. Set these before running:

| Variable  | Description                                | Default     |
|-----------|--------------------------------------------|-------------|
| `DB_HOST` | SQL Server hostname or IP                  | `localhost` |
| `DB_PORT` | SQL Server TCP port                        | `1433`      |
| `DB_NAME` | Database name                              | `BookLoopDB`|
| `DB_USER` | SQL Server login username                  | `sa`        |
| `DB_PASS` | SQL Server login password                  | *(none)*    |

### Setting environment variables (Windows PowerShell)

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "1433"
$env:DB_NAME = "BookLoopDB"
$env:DB_USER = "sa"
$env:DB_PASS = "YourPasswordHere"
```

### Setting environment variables (Windows Command Prompt)

```cmd
set DB_HOST=localhost
set DB_PORT=1433
set DB_NAME=BookLoopDB
set DB_USER=sa
set DB_PASS=YourPasswordHere
```

> **Security:** Never commit real credentials. The `application.properties` file uses `${DB_PASS:}` placeholder syntax.

---

## Running the Application

### Using the Maven wrapper (recommended)

```powershell
# Navigate to the project directory
cd bookloop

# Build and run
.\mvnw.cmd spring-boot:run
```

### With demo seed data

To auto-populate the database with fictional sample data on first run:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

### With explicit Maven

```powershell
mvn spring-boot:run
```

After startup, open your browser to:

```
http://localhost:8080
```

### Demo accounts (when using dev profile)

| Email                  | Password      |
|------------------------|---------------|
| alice@university.edu   | password123   |
| bob@university.edu     | password123   |
| carol@university.edu   | password123   |

---

## Running Tests

Tests use an **H2 in-memory database** — no SQL Server connection is required.

```powershell
.\mvnw.cmd clean test
```

### Test categories

| Test Class                  | Tests | Description                              |
|-----------------------------|-------|------------------------------------------|
| `BookloopApplicationTests`  | 1     | Spring context loads successfully         |
| `UserServiceTest`           | 6     | Registration, BCrypt, duplicate email, role |
| `ProductServiceTest`        | 11    | CRUD, ownership, search, filters          |
| `SecurityIntegrationTest`   | 8     | Public/protected page access rules        |

---

## Main Routes

### Public (no login required)

| Route              | Method | Description                         |
|--------------------|--------|-------------------------------------|
| `/`                | GET    | Homepage with recent listings       |
| `/books`           | GET    | Browse/search all available books   |
| `/books/{id}`      | GET    | Book detail page                    |
| `/login`           | GET    | Login page                          |
| `/login`           | POST   | Process login                       |
| `/register`        | GET    | Registration page                   |
| `/register`        | POST   | Process registration                |
| `/profile/seller/{id}` | GET | Public seller profile              |

### Authenticated (login required)

| Route                     | Method | Description                    |
|---------------------------|--------|--------------------------------|
| `/profile`                | GET    | View/edit personal profile     |
| `/profile/update`         | POST   | Update profile                 |
| `/my-listings`            | GET    | View your listings             |
| `/my-listings/new`        | GET    | Create listing form            |
| `/my-listings/new`        | POST   | Submit new listing             |
| `/my-listings/{id}/edit`  | GET    | Edit listing form              |
| `/my-listings/{id}/edit`  | POST   | Submit listing update          |
| `/my-listings/{id}/delete`| GET    | Delete confirmation page       |
| `/my-listings/{id}/delete`| POST   | Confirm and delete listing     |
| `/logout`                 | POST   | Sign out                       |

---

## Project Structure

```
src/main/java/com/bookloop/
├── BookloopApplication.java        # Spring Boot entry point
├── config/
│   ├── SecurityConfig.java         # Spring Security configuration
│   ├── AuthenticatedUserResolver.java  # Helper to get current user
│   └── DataSeeder.java             # Demo data (dev profile only)
├── controller/
│   ├── HomeController.java         # Homepage
│   ├── AuthController.java         # Login & registration
│   ├── BookController.java         # Browse & detail (public)
│   ├── ProfileController.java      # Profile & seller pages
│   ├── ListingController.java      # CRUD for listings
│   └── GlobalExceptionHandler.java # Error handling
├── dto/
│   ├── RegistrationForm.java       # Registration form validation
│   ├── ProfileUpdateForm.java      # Profile edit form
│   └── ProductForm.java            # Listing create/edit form
├── entity/
│   ├── User.java                   # User entity
│   ├── Product.java                # Product (book listing) entity
│   ├── Role.java                   # User role enum
│   ├── Condition.java              # Book condition enum
│   └── ListingStatus.java          # Listing status enum
├── exception/
│   ├── DuplicateEmailException.java
│   ├── ListingNotFoundException.java
│   └── UnauthorizedOperationException.java
├── repository/
│   ├── UserRepository.java
│   └── ProductRepository.java
└── service/
    ├── UserService.java
    ├── ProductService.java
    └── BookloopUserDetailsService.java

src/main/resources/
├── application.properties
├── static/
│   ├── css/bookloop.css
│   └── js/bookloop.js
└── templates/
    ├── layout/layout.html          # Shared navbar, footer, head
    ├── home.html                   # Homepage
    ├── auth/login.html
    ├── auth/register.html
    ├── books/list.html             # Browse/search
    ├── books/detail.html           # Book detail
    ├── profile/view.html           # User profile
    ├── profile/seller.html         # Public seller profile
    ├── listings/my-listings.html   # My listings
    ├── listings/form.html          # Create/edit listing
    ├── listings/delete-confirm.html
    ├── error.html                  # Default error page
    └── error/
        ├── 400.html
        ├── 403.html
        └── 404.html

sql/
└── schema.sql                      # SQL Server schema script
```

---

## Known Limitations

- **Phase 3 only** — No order processing, payments, chat, reviews, or admin dashboard.
- **Image URLs only** — Book images are specified by URL; no file upload feature yet.
- **No password reset** — Users cannot recover or change their password in this phase.
- **No email verification** — Registration does not verify the email address.
- **Session-based auth** — No JWT or remember-me token.
- **Single sort order** — Listings are sorted by newest first; no user-selectable sorting.

---

## Future Phases

- **Phase 4** — Purchase requests, buyer/seller messaging, transaction flow.
- **Phase 5** — Reviews and ratings, seller reputation system.
- **Phase 6** — Admin dashboard, content moderation, analytics.
- **Phase 7** — Image upload, email notifications, password reset, social login.

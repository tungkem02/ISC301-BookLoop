-- =========================================================================
-- BookLoop — SQL Server Database Schema
-- =========================================================================
-- This script creates the BookLoopDB database and all required tables.
-- Run this script in SQL Server Management Studio (SSMS) or sqlcmd
-- BEFORE starting the application for the first time.
--
-- Note: Hibernate (ddl-auto=update) will also create/update tables
-- automatically, but this script is provided for manual setup,
-- documentation, and production environments.
-- =========================================================================

-- 1. Create the database (skip if it already exists)
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'BookLoopDB')
BEGIN
    CREATE DATABASE BookLoopDB;
END
GO

USE BookLoopDB;
GO

-- =========================================================================
-- 2. Users table
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'users') AND type = 'U')
BEGIN
    CREATE TABLE users (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        full_name       NVARCHAR(100)   NOT NULL,
        email           NVARCHAR(150)   NOT NULL,
        password        NVARCHAR(255)   NOT NULL,
        university      NVARCHAR(150)   NULL,
        campus          NVARCHAR(100)   NULL,
        phone           NVARCHAR(20)    NULL,
        avatar_url      NVARCHAR(500)   NULL,
        role            VARCHAR(20)     NOT NULL DEFAULT 'STUDENT',
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),
        updated_at      DATETIME2       NULL,

        CONSTRAINT UQ_users_email UNIQUE (email)
    );

    -- Index for email lookups (login)
    CREATE NONCLUSTERED INDEX IX_users_email ON users (email);
END
GO

-- =========================================================================
-- 3. Products table (book listings)
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'products') AND type = 'U')
BEGIN
    CREATE TABLE products (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        title           NVARCHAR(255)   NOT NULL,
        description     NVARCHAR(3000)  NULL,
        price           DECIMAL(12, 2)  NOT NULL,
        condition_grade VARCHAR(20)     NOT NULL,
        category        NVARCHAR(100)   NULL,
        author          NVARCHAR(150)   NULL,
        publisher       NVARCHAR(150)   NULL,
        edition         NVARCHAR(50)    NULL,
        isbn            VARCHAR(20)     NULL,
        course_code     VARCHAR(20)     NULL,
        image_url       NVARCHAR(500)   NULL,
        defect_notes    NVARCHAR(1000)  NULL,
        campus          NVARCHAR(100)   NOT NULL,
        status          VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),
        updated_at      DATETIME2       NULL,
        seller_id       BIGINT          NOT NULL,

        CONSTRAINT FK_products_seller FOREIGN KEY (seller_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

        CONSTRAINT CK_products_price CHECK (price >= 0),
        CONSTRAINT CK_products_condition CHECK (condition_grade IN ('NEW','LIKE_NEW','GOOD','ACCEPTABLE')),
        CONSTRAINT CK_products_status CHECK (status IN ('AVAILABLE','SOLD','HIDDEN'))
    );

    -- Indexes for filtering and search performance
    CREATE NONCLUSTERED INDEX IX_products_status    ON products (status);
    CREATE NONCLUSTERED INDEX IX_products_seller    ON products (seller_id);
    CREATE NONCLUSTERED INDEX IX_products_category  ON products (category);
    CREATE NONCLUSTERED INDEX IX_products_campus    ON products (campus);
    CREATE NONCLUSTERED INDEX IX_products_created   ON products (created_at DESC);
END
GO

-- =========================================================================
-- 4. (Optional) Sample data for testing — clearly fictional
-- =========================================================================
-- Uncomment and run the section below ONLY if you want to seed the database
-- manually. Alternatively, use the DataSeeder by running the app with
-- the "dev" Spring profile: -Dspring.profiles.active=dev
-- =========================================================================

/*
-- Demo user (password = "password123" hashed with BCrypt)
INSERT INTO users (full_name, email, password, university, campus, role)
VALUES (
    N'Demo Student',
    N'demo@university.edu',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    N'State University',
    N'Main Campus',
    'STUDENT'
);

-- Demo listing
INSERT INTO products (title, author, publisher, price, condition_grade, category,
                      campus, status, seller_id)
VALUES (
    N'Introduction to Algorithms',
    N'Thomas H. Cormen',
    N'MIT Press',
    55.00,
    'GOOD',
    N'Computer Science',
    N'Main Campus Library',
    'AVAILABLE',
    1
);
*/

PRINT 'BookLoopDB schema created successfully.';
GO

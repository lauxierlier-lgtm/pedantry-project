CREATE DATABASE IF NOT EXISTS pedantry;
USE pedantry;
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(10) DEFAULT 'USER',
    num_uploads INT DEFAULT 0
);

CREATE TABLE documents (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,  -- HTML content as text
    uploader_id INT NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploader_id) REFERENCES users(id)
);

-- Sample data (inserted on startup)
INSERT INTO users (username, password, email, role) VALUES 
('admin', 'adminpass', 'admin@college.edu', 'ADMIN'),
('student1', 'pass123', 'student1@college.edu', 'USER');
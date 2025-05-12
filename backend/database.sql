-- Création de la base de données
CREATE DATABASE IF NOT EXISTS student_db;
USE student_db;

-- Table des étudiants
CREATE TABLE IF NOT EXISTS STUDENTS (
    students_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL
);

-- Table des cours
CREATE TABLE IF NOT EXISTS courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    course_field VARCHAR(100) NOT NULL,
    course_duration VARCHAR(50) NOT NULL,
    course_price DECIMAL(10, 2) NOT NULL
); 
-- Table des administrateurs
CREATE TABLE IF NOT EXISTS admins (
  admin_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des professeurs
CREATE TABLE IF NOT EXISTS professors (
  professor_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  department VARCHAR(100),
  password VARCHAR(255) NOT NULL
);

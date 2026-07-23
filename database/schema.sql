CREATE DATABASE IF NOT EXISTS quizsphere
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE quizsphere;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  password VARCHAR(255) NOT NULL,
  auth_provider VARCHAR(20) DEFAULT 'LOCAL',
  google_subject VARCHAR(255),
  avatar_url VARCHAR(1000),
  bio VARCHAR(500),
  phone VARCHAR(30),
  location VARCHAR(120),
  date_of_birth DATE,
  security_question VARCHAR(255),
  security_answer_hash VARCHAR(100),
  recovery_code_hash VARCHAR(100),
  recovery_failed_attempts INT NOT NULL DEFAULT 0,
  recovery_locked_until TIMESTAMP(6) NULL,
  security_question_updated_at TIMESTAMP(6) NULL,
  role VARCHAR(20) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT uk_users_email UNIQUE (email),
  CONSTRAINT uk_users_google_subject UNIQUE (google_subject)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS password_recovery_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  challenge_token_hash VARCHAR(64) NULL,
  challenge_expires_at TIMESTAMP(6) NULL,
  answer_verified BOOLEAN NOT NULL DEFAULT FALSE,
  recovery_code_verified BOOLEAN NOT NULL DEFAULT FALSE,
  reset_token_hash VARCHAR(64) NULL,
  reset_token_expires_at TIMESTAMP(6) NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uk_recovery_challenge_hash UNIQUE (challenge_token_hash),
  CONSTRAINT uk_recovery_reset_hash UNIQUE (reset_token_hash),
  CONSTRAINT fk_recovery_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_recovery_session_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  description VARCHAR(500),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS quizzes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(150) NOT NULL,
  description VARCHAR(1000),
  difficulty VARCHAR(20) NOT NULL,
  duration_minutes INT NOT NULL,
  pass_percentage INT NOT NULL,
  published BOOLEAN NOT NULL DEFAULT FALSE,
  category_id BIGINT NOT NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_quiz_category FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT fk_quiz_creator FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS questions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question_text TEXT NOT NULL,
  explanation TEXT,
  marks INT NOT NULL DEFAULT 1,
  quiz_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_question_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS question_options (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  option_text VARCHAR(1000) NOT NULL,
  correct BOOLEAN NOT NULL DEFAULT FALSE,
  question_id BIGINT NOT NULL,
  CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS quiz_attempts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  quiz_id BIGINT NOT NULL,
  score INT NOT NULL DEFAULT 0,
  total_marks INT NOT NULL DEFAULT 0,
  percentage DOUBLE NOT NULL DEFAULT 0,
  passed BOOLEAN NOT NULL DEFAULT FALSE,
  started_at TIMESTAMP(6) NOT NULL,
  submitted_at TIMESTAMP(6) NULL,
  status VARCHAR(20) NOT NULL,
  CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_attempt_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
  INDEX idx_attempt_user (user_id),
  INDEX idx_attempt_quiz (quiz_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attempt_answers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  attempt_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  selected_option_id BIGINT NULL,
  correct BOOLEAN NOT NULL DEFAULT FALSE,
  marks_awarded INT NOT NULL DEFAULT 0,
  CONSTRAINT uk_attempt_question UNIQUE (attempt_id, question_id),
  CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
  CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES questions(id),
  CONSTRAINT fk_answer_option FOREIGN KEY (selected_option_id) REFERENCES question_options(id)
) ENGINE=InnoDB;

-- Optional manual migration. Not required when JPA_DDL_AUTO=update succeeds.
ALTER TABLE users
  ADD COLUMN security_question VARCHAR(255) NULL,
  ADD COLUMN security_answer_hash VARCHAR(100) NULL,
  ADD COLUMN recovery_code_hash VARCHAR(100) NULL,
  ADD COLUMN recovery_failed_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN recovery_locked_until TIMESTAMP(6) NULL,
  ADD COLUMN security_question_updated_at TIMESTAMP(6) NULL;

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

-- =====================================================
-- Flyway Migration V13: 고정지출 테이블 생성
-- =====================================================
-- 매월 특정 날짜에 자동으로 지출을 등록하기 위한 템플릿 테이블

CREATE TABLE IF NOT EXISTS `recurring_expenses` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `uuid`                 VARCHAR(36)  NOT NULL UNIQUE,
    `family_uuid`          VARCHAR(36)  NOT NULL,
    `category_uuid`        VARCHAR(36)  NOT NULL,
    `user_uuid`            VARCHAR(36)  NOT NULL COMMENT '등록자 UUID',
    `amount`               DECIMAL(12, 2) NOT NULL,
    `description`          TEXT,
    `day_of_month`         TINYINT      NOT NULL COMMENT '매월 등록 날짜 (1~28)',
    `exclude_from_budget`  TINYINT(1)   NOT NULL DEFAULT 0,
    `status`               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE | DELETED',
    `created_at`           DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`           DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX `idx_recurring_expenses_family_uuid` (`family_uuid`),
    INDEX `idx_recurring_expenses_day`         (`day_of_month`, `status`),
    CONSTRAINT `fk_recurring_expenses_family`   FOREIGN KEY (`family_uuid`)   REFERENCES `families`(`uuid`)    ON DELETE CASCADE,
    CONSTRAINT `fk_recurring_expenses_category` FOREIGN KEY (`category_uuid`) REFERENCES `categories`(`uuid`) ON DELETE RESTRICT,
    CONSTRAINT `fk_recurring_expenses_user`     FOREIGN KEY (`user_uuid`)     REFERENCES `users`(`uuid`)      ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

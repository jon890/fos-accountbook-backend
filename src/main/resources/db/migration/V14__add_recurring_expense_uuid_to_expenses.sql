-- =====================================================
-- Flyway Migration V14: expenses 테이블에 recurring_expense_uuid 컬럼 추가
-- =====================================================
-- 스케줄러가 생성한 지출을 식별하고 월별 중복 생성을 방지하기 위한 컬럼

ALTER TABLE `expenses`
    ADD COLUMN `recurring_expense_uuid` VARCHAR(36) NULL COMMENT '고정지출 UUID (스케줄러 생성 지출인 경우)',
    ADD INDEX `idx_expenses_recurring_uuid` (`recurring_expense_uuid`);

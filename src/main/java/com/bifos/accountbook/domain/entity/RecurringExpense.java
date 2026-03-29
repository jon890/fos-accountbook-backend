package com.bifos.accountbook.domain.entity;

import com.bifos.accountbook.domain.value.CustomUuid;
import com.bifos.accountbook.domain.value.RecurringExpenseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 고정지출 엔티티
 * 매월 특정 날짜에 자동으로 등록될 지출 템플릿입니다.
 */
@Entity
@Table(name = "recurring_expenses", indexes = {
    @Index(name = "idx_recurring_expenses_family_uuid", columnList = "family_uuid"),
    @Index(name = "idx_recurring_expenses_day", columnList = "day_of_month,status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RecurringExpense {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 36)
  private CustomUuid uuid;

  @Column(name = "family_uuid", nullable = false, length = 36)
  private CustomUuid familyUuid;

  @Column(name = "category_uuid", nullable = false, length = 36)
  private CustomUuid categoryUuid;

  @Column(name = "user_uuid", nullable = false, length = 36)
  private CustomUuid userUuid;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(columnDefinition = "TEXT")
  private String description;

  /**
   * 매월 지출이 등록될 날짜 (1~28)
   * 월마다 일수가 다르므로 최대 28일로 제한합니다.
   */
  @Column(name = "day_of_month", nullable = false)
  private int dayOfMonth;

  @Column(name = "exclude_from_budget", nullable = false)
  private boolean excludeFromBudget = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RecurringExpenseStatus status = RecurringExpenseStatus.ACTIVE;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    if (uuid == null) {
      uuid = CustomUuid.generate();
    }
  }

  // ========== 비즈니스 메서드 ==========

  /**
   * 고정지출 정보 수정
   */
  public void update(CustomUuid categoryUuid, BigDecimal amount, String description,
                     Integer dayOfMonth, Boolean excludeFromBudget) {
    if (categoryUuid != null) {
      this.categoryUuid = categoryUuid;
    }
    if (amount != null) {
      this.amount = amount;
    }
    if (description != null) {
      this.description = description;
    }
    if (dayOfMonth != null) {
      this.dayOfMonth = dayOfMonth;
    }
    if (excludeFromBudget != null) {
      this.excludeFromBudget = excludeFromBudget;
    }
  }

  /**
   * 고정지출 삭제 (Soft Delete)
   */
  public void delete() {
    this.status = RecurringExpenseStatus.DELETED;
  }
}

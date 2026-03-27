package com.bifos.accountbook.infra.persistence.repository.jpa;

import com.bifos.accountbook.domain.entity.RecurringExpense;
import com.bifos.accountbook.domain.value.CustomUuid;
import com.bifos.accountbook.domain.value.RecurringExpenseStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 고정지출 JPA Repository
 */
public interface RecurringExpenseJpaRepository extends JpaRepository<RecurringExpense, Long> {

  Optional<RecurringExpense> findByUuidAndStatus(CustomUuid uuid, RecurringExpenseStatus status);

  List<RecurringExpense> findAllByFamilyUuidAndStatus(CustomUuid familyUuid, RecurringExpenseStatus status);

  List<RecurringExpense> findAllByDayOfMonthAndStatus(int dayOfMonth, RecurringExpenseStatus status);
}

package com.bifos.accountbook.infra.persistence.repository.impl;

import com.bifos.accountbook.domain.entity.RecurringExpense;
import com.bifos.accountbook.domain.repository.RecurringExpenseRepository;
import com.bifos.accountbook.domain.value.CustomUuid;
import com.bifos.accountbook.domain.value.RecurringExpenseStatus;
import com.bifos.accountbook.infra.persistence.repository.jpa.RecurringExpenseJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * RecurringExpenseRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class RecurringExpenseRepositoryImpl implements RecurringExpenseRepository {

  private final RecurringExpenseJpaRepository jpaRepository;

  @Override
  public RecurringExpense save(RecurringExpense recurringExpense) {
    return jpaRepository.save(recurringExpense);
  }

  @Override
  public Optional<RecurringExpense> findActiveByUuid(CustomUuid uuid) {
    return jpaRepository.findByUuidAndStatus(uuid, RecurringExpenseStatus.ACTIVE);
  }

  @Override
  public List<RecurringExpense> findAllActiveByFamilyUuid(CustomUuid familyUuid) {
    return jpaRepository.findAllByFamilyUuidAndStatus(familyUuid, RecurringExpenseStatus.ACTIVE);
  }

  @Override
  public List<RecurringExpense> findAllActiveByDayOfMonth(int dayOfMonth) {
    return jpaRepository.findAllByDayOfMonthAndStatus(dayOfMonth, RecurringExpenseStatus.ACTIVE);
  }
}

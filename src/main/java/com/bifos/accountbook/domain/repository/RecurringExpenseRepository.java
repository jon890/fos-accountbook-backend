package com.bifos.accountbook.domain.repository;

import com.bifos.accountbook.domain.entity.RecurringExpense;
import com.bifos.accountbook.domain.value.CustomUuid;
import java.util.List;
import java.util.Optional;

/**
 * 고정지출 Repository 인터페이스
 */
public interface RecurringExpenseRepository {

  RecurringExpense save(RecurringExpense recurringExpense);

  Optional<RecurringExpense> findActiveByUuid(CustomUuid uuid);

  /**
   * 가족의 모든 활성 고정지출 조회
   */
  List<RecurringExpense> findAllActiveByFamilyUuid(CustomUuid familyUuid);

  /**
   * 스케줄러용: 특정 날짜(1~28)에 해당하는 모든 활성 고정지출 조회
   */
  List<RecurringExpense> findAllActiveByDayOfMonth(int dayOfMonth);
}

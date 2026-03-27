package com.bifos.accountbook.presentation.controller;

import com.bifos.accountbook.application.dto.expense.CreateRecurringExpenseRequest;
import com.bifos.accountbook.application.dto.expense.RecurringExpenseResponse;
import com.bifos.accountbook.application.dto.expense.UpdateRecurringExpenseRequest;
import com.bifos.accountbook.application.service.RecurringExpenseService;
import com.bifos.accountbook.domain.value.CustomUuid;
import com.bifos.accountbook.presentation.annotation.LoginUser;
import com.bifos.accountbook.presentation.dto.ApiSuccessResponse;
import com.bifos.accountbook.presentation.dto.LoginUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "고정지출 (RecurringExpense)", description = "매월 자동 등록되는 고정지출 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RecurringExpenseController {

  private final RecurringExpenseService recurringExpenseService;

  @Operation(summary = "고정지출 등록", description = "매월 특정 날짜에 자동 등록될 고정지출을 생성합니다.")
  @ApiResponse(responseCode = "201", description = "등록 성공")
  @ApiResponse(responseCode = "403", description = "접근 권한 없음")
  @PostMapping("/families/{familyUuid}/recurring-expenses")
  public ResponseEntity<ApiSuccessResponse<RecurringExpenseResponse>> createRecurringExpense(
      @LoginUser LoginUserDto loginUser,
      @Parameter(description = "가족 UUID") @PathVariable CustomUuid familyUuid,
      @Valid @RequestBody CreateRecurringExpenseRequest request) {
    log.info("Creating recurring expense for family: {} by user: {}", familyUuid.getValue(),
        loginUser.userUuid());

    RecurringExpenseResponse response = recurringExpenseService.createRecurringExpense(
        loginUser.userUuid(), familyUuid, request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiSuccessResponse.of("고정지출이 등록되었습니다", response));
  }

  @Operation(summary = "고정지출 목록 조회", description = "가족의 모든 활성 고정지출 목록을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping("/families/{familyUuid}/recurring-expenses")
  public ResponseEntity<ApiSuccessResponse<List<RecurringExpenseResponse>>> getRecurringExpenses(
      @LoginUser LoginUserDto loginUser,
      @Parameter(description = "가족 UUID") @PathVariable CustomUuid familyUuid) {
    List<RecurringExpenseResponse> responses = recurringExpenseService.getRecurringExpenses(
        loginUser.userUuid(), familyUuid);

    return ResponseEntity.ok(ApiSuccessResponse.of(responses));
  }

  @Operation(summary = "고정지출 수정", description = "고정지출 정보를 수정합니다.")
  @ApiResponse(responseCode = "200", description = "수정 성공")
  @ApiResponse(responseCode = "403", description = "접근 권한 없음")
  @ApiResponse(responseCode = "404", description = "고정지출을 찾을 수 없음")
  @PutMapping("/families/{familyUuid}/recurring-expenses/{recurringExpenseUuid}")
  public ResponseEntity<ApiSuccessResponse<RecurringExpenseResponse>> updateRecurringExpense(
      @LoginUser LoginUserDto loginUser,
      @Parameter(description = "가족 UUID") @PathVariable CustomUuid familyUuid,
      @Parameter(description = "고정지출 UUID") @PathVariable CustomUuid recurringExpenseUuid,
      @Valid @RequestBody UpdateRecurringExpenseRequest request) {
    log.info("Updating recurring expense: {} in family: {} by user: {}",
        recurringExpenseUuid.getValue(), familyUuid.getValue(), loginUser.userUuid());

    RecurringExpenseResponse response = recurringExpenseService.updateRecurringExpense(
        loginUser.userUuid(), familyUuid, recurringExpenseUuid, request);

    return ResponseEntity.ok(ApiSuccessResponse.of("고정지출이 수정되었습니다", response));
  }

  @Operation(summary = "고정지출 삭제", description = "고정지출을 삭제합니다. 이미 등록된 지출 내역은 유지됩니다.")
  @ApiResponse(responseCode = "200", description = "삭제 성공")
  @ApiResponse(responseCode = "403", description = "접근 권한 없음")
  @ApiResponse(responseCode = "404", description = "고정지출을 찾을 수 없음")
  @DeleteMapping("/families/{familyUuid}/recurring-expenses/{recurringExpenseUuid}")
  public ResponseEntity<ApiSuccessResponse<Void>> deleteRecurringExpense(
      @LoginUser LoginUserDto loginUser,
      @Parameter(description = "가족 UUID") @PathVariable CustomUuid familyUuid,
      @Parameter(description = "고정지출 UUID") @PathVariable CustomUuid recurringExpenseUuid) {
    log.info("Deleting recurring expense: {} in family: {} by user: {}",
        recurringExpenseUuid.getValue(), familyUuid.getValue(), loginUser.userUuid());

    recurringExpenseService.deleteRecurringExpense(loginUser.userUuid(), familyUuid, recurringExpenseUuid);

    return ResponseEntity.ok(ApiSuccessResponse.of("고정지출이 삭제되었습니다", null));
  }
}

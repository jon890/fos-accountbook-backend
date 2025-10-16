package com.bifos.accountbook.presentation.controller;

import com.bifos.accountbook.application.dto.ApiSuccessResponse;
import com.bifos.accountbook.application.dto.invitation.AcceptInvitationRequest;
import com.bifos.accountbook.application.dto.invitation.CreateInvitationRequest;
import com.bifos.accountbook.application.dto.invitation.InvitationResponse;
import com.bifos.accountbook.application.service.InvitationService;
import com.bifos.accountbook.presentation.annotation.LoginUser;
import com.bifos.accountbook.presentation.dto.LoginUserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * 초대장 생성
     */
    @PostMapping("/families/{familyUuid}")
    public ResponseEntity<ApiSuccessResponse<InvitationResponse>> createInvitation(
            @LoginUser LoginUserDto loginUser,
            @PathVariable String familyUuid,
            @Valid @RequestBody(required = false) CreateInvitationRequest request) {
        log.info("Creating invitation for family: {} by user: {}", familyUuid, loginUser.getUserUuid());

        if (request == null) {
            request = new CreateInvitationRequest(72); // 기본 3일
        }

        InvitationResponse response = invitationService.createInvitation(loginUser.getUserUuid(), familyUuid, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.of("초대장이 생성되었습니다", response));
    }

    /**
     * 가족의 활성 초대장 목록 조회
     */
    @GetMapping("/families/{familyUuid}")
    public ResponseEntity<ApiSuccessResponse<List<InvitationResponse>>> getFamilyInvitations(
            @LoginUser LoginUserDto loginUser,
            @PathVariable String familyUuid) {
        log.info("Fetching invitations for family: {} by user: {}", familyUuid, loginUser.getUserUuid());

        List<InvitationResponse> invitations = invitationService.getFamilyInvitations(loginUser.getUserUuid(),
                familyUuid);

        return ResponseEntity.ok(ApiSuccessResponse.of(invitations));
    }

    /**
     * 초대장 정보 조회 (공개 API - 토큰으로 조회)
     */
    @GetMapping("/token/{token}")
    public ResponseEntity<ApiSuccessResponse<InvitationResponse>> getInvitationByToken(
            @PathVariable String token) {
        log.info("Fetching invitation by token: {}", token);

        InvitationResponse invitation = invitationService.getInvitationByToken(token);

        return ResponseEntity.ok(ApiSuccessResponse.of(invitation));
    }

    /**
     * 초대 수락
     */
    @PostMapping("/accept")
    public ResponseEntity<ApiSuccessResponse<Void>> acceptInvitation(
            @LoginUser LoginUserDto loginUser,
            @Valid @RequestBody AcceptInvitationRequest request) {
        log.info("User: {} accepting invitation with token: {}", loginUser.getUserUuid(), request.getToken());

        invitationService.acceptInvitation(loginUser.getUserUuid(), request.getToken());

        return ResponseEntity.ok(ApiSuccessResponse.of("초대를 수락했습니다. 가족에 가입되었습니다.", null));
    }

    /**
     * 초대장 삭제
     */
    @DeleteMapping("/{invitationUuid}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteInvitation(
            @LoginUser LoginUserDto loginUser,
            @PathVariable String invitationUuid) {
        log.info("Deleting invitation: {} by user: {}", invitationUuid, loginUser.getUserUuid());

        invitationService.deleteInvitation(loginUser.getUserUuid(), invitationUuid);

        return ResponseEntity.ok(ApiSuccessResponse.of("초대장이 삭제되었습니다", null));
    }
}

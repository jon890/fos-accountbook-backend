package com.bifos.accountbook.application.service;

import com.bifos.accountbook.application.dto.category.CategoryResponse;
import com.bifos.accountbook.application.dto.category.CreateCategoryRequest;
import com.bifos.accountbook.application.dto.category.UpdateCategoryRequest;
import com.bifos.accountbook.domain.entity.Category;
import com.bifos.accountbook.domain.repository.CategoryRepository;
import com.bifos.accountbook.domain.repository.FamilyMemberRepository;
import com.bifos.accountbook.domain.repository.UserRepository;
import com.bifos.accountbook.domain.value.CustomUuid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;

    /**
     * 카테고리 생성
     */
    @Transactional
    public CategoryResponse createCategory(CustomUuid userUuid, String familyUuid, CreateCategoryRequest request) {
        CustomUuid familyCustomUuid = CustomUuid.from(familyUuid);

        // 권한 확인
        validateFamilyAccess(userUuid, familyCustomUuid);

        // 중복 확인
        categoryRepository.findByFamilyUuidAndName(familyCustomUuid, request.getName())
                .ifPresent(c -> {
                    throw new IllegalStateException("이미 존재하는 카테고리 이름입니다");
                });

        // 카테고리 생성
        Category category = Category.builder()
                .familyUuid(familyCustomUuid)
                .name(request.getName())
                .color(request.getColor() != null ? request.getColor() : "#6366f1")
                .icon(request.getIcon())
                .build();

        category = categoryRepository.save(category);
        log.info("Created category: {} in family: {} by user: {}", category.getUuid(), familyUuid, userUuid);

        return CategoryResponse.from(category);
    }

    /**
     * 가족의 카테고리 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getFamilyCategories(CustomUuid userUuid, String familyUuid) {
        CustomUuid familyCustomUuid = CustomUuid.from(familyUuid);

        // 권한 확인
        validateFamilyAccess(userUuid, familyCustomUuid);

        List<Category> categories = categoryRepository.findAllByFamilyUuid(familyCustomUuid);

        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 상세 조회
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(CustomUuid userUuid, String categoryUuid) {
        CustomUuid categoryCustomUuid = CustomUuid.from(categoryUuid);

        Category category = categoryRepository.findActiveByUuid(categoryCustomUuid)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        // 권한 확인
        validateFamilyAccess(userUuid, category.getFamilyUuid());

        return CategoryResponse.from(category);
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public CategoryResponse updateCategory(CustomUuid userUuid, String categoryUuid, UpdateCategoryRequest request) {
        CustomUuid categoryCustomUuid = CustomUuid.from(categoryUuid);

        Category category = categoryRepository.findActiveByUuid(categoryCustomUuid)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        // 권한 확인
        validateFamilyAccess(userUuid, category.getFamilyUuid());

        // 이름 변경 시 중복 확인
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            categoryRepository.findByFamilyUuidAndName(category.getFamilyUuid(), request.getName())
                    .ifPresent(c -> {
                        throw new IllegalStateException("이미 존재하는 카테고리 이름입니다");
                    });
            category.setName(request.getName());
        }

        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }

        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }

        category = categoryRepository.save(category);
        log.info("Updated category: {} by user: {}", categoryUuid, userUuid);

        return CategoryResponse.from(category);
    }

    /**
     * 카테고리 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteCategory(CustomUuid userUuid, String categoryUuid) {
        CustomUuid categoryCustomUuid = CustomUuid.from(categoryUuid);

        Category category = categoryRepository.findActiveByUuid(categoryCustomUuid)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        // 권한 확인
        validateFamilyAccess(userUuid, category.getFamilyUuid());

        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);

        log.info("Deleted category: {} by user: {}", categoryUuid, userUuid);
    }

    /**
     * 가족 생성 시 기본 카테고리 자동 생성
     * FamilyService에서 호출됨 (권한 검증 불필요 - 가족 생성 시점)
     */
    @Transactional
    public void createDefaultCategoriesForFamily(CustomUuid familyUuid) {
        List<DefaultCategory> defaultCategories = Arrays.asList(
                new DefaultCategory("식비", "#ef4444", "🍚"),
                new DefaultCategory("카페", "#f59e0b", "☕"),
                new DefaultCategory("간식", "#ec4899", "🍰"),
                new DefaultCategory("생활비", "#10b981", "🏠"),
                new DefaultCategory("교통비", "#3b82f6", "🚗"),
                new DefaultCategory("쇼핑", "#8b5cf6", "🛍️"),
                new DefaultCategory("의료", "#06b6d4", "💊"),
                new DefaultCategory("문화생활", "#f43f5e", "🎬"),
                new DefaultCategory("교육", "#14b8a6", "📚"),
                new DefaultCategory("기타", "#6b7280", "📦"));

        for (DefaultCategory defaultCategory : defaultCategories) {
            Category category = Category.builder()
                    .familyUuid(familyUuid)
                    .name(defaultCategory.name)
                    .color(defaultCategory.color)
                    .icon(defaultCategory.icon)
                    .build();

            categoryRepository.save(category);
        }

        log.info("Created {} default categories for family: {}", defaultCategories.size(), familyUuid);
    }

    /**
     * 가족 접근 권한 확인
     */
    private void validateFamilyAccess(CustomUuid userUuid, CustomUuid familyUuid) {
        boolean isMember = familyMemberRepository.existsByFamilyUuidAndUserUuidAndDeletedAtIsNull(
                familyUuid, userUuid);

        if (!isMember) {
            throw new IllegalStateException("해당 가족에 접근할 권한이 없습니다");
        }
    }

    /**
     * 기본 카테고리 정보를 담는 내부 클래스
     */
    private static class DefaultCategory {
        String name;
        String color;
        String icon;

        DefaultCategory(String name, String color, String icon) {
            this.name = name;
            this.color = color;
            this.icon = icon;
        }
    }
}

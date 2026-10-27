package com.ledger.category;

import com.ledger.common.ApiException;
import com.ledger.transaction.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * System presets are merged with the user's own categories.
     * Optionally filter by type.
     */
    public List<CategoryDto> list(Long userId, CategoryType type) {
        List<Category> categories = new ArrayList<>();
        categories.addAll(categoryRepository.findByUserIdIsNullOrderBySortOrderAscIdAsc());
        categories.addAll(categoryRepository.findByUserIdOrderBySortOrderAscIdAsc(userId));

        return categories.stream()
                .filter(c -> type == null || c.getType() == type)
                .sorted(Comparator.comparing(Category::getSortOrder,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Category::getId))
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }

    public CategoryDto create(Long userId, CategoryRequest request) {
        Category category = Category.builder()
                .userId(userId)
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon())
                .sortOrder(request.getSortOrder())
                .build();
        return CategoryDto.from(categoryRepository.save(category));
    }

    public CategoryDto update(Long userId, Long id, CategoryRequest request) {
        Category category = getOwnedOrThrow(userId, id);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon());
        category.setSortOrder(request.getSortOrder());
        return CategoryDto.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Category category = getOwnedOrThrow(userId, id);
        // Detach any transactions still referencing this category.
        transactionRepository.detachCategory(id);
        categoryRepository.delete(category);
    }

    private Category getOwnedOrThrow(Long userId, Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Category not found"));
        if (category.getUserId() == null || !category.getUserId().equals(userId)) {
            throw ApiException.notFound("Category not found");
        }
        return category;
    }

    /**
     * Resolves the category referenced by a transaction, enforcing ownership
     * (system presets or the user's own categories) and type consistency.
     * Returns null when no category is provided.
     */
    public Category resolveForTransaction(Long userId, Long categoryId, CategoryType type) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ApiException.badRequest("Category not found"));
        boolean accessible = category.getUserId() == null || category.getUserId().equals(userId);
        if (!accessible) {
            throw ApiException.badRequest("Category not found");
        }
        if (category.getType() != type) {
            throw ApiException.badRequest("Category type does not match transaction type");
        }
        return category;
    }
}

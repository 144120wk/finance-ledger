package com.ledger.category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {

    private Long id;
    private Long userId;
    private String name;
    private CategoryType type;
    private String icon;
    private Integer sortOrder;
    private boolean builtin;

    public static CategoryDto from(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .userId(category.getUserId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .sortOrder(category.getSortOrder())
                .builtin(category.getUserId() == null)
                .build();
    }
}

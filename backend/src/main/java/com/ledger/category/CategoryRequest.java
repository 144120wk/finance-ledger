package com.ledger.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name is too long")
    private String name;

    @NotNull(message = "type is required")
    private CategoryType type;

    @Size(max = 50, message = "icon is too long")
    private String icon;

    private Integer sortOrder;
}

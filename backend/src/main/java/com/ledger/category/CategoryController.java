package com.ledger.category;

import com.ledger.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryDto>> list(@RequestAttribute("userId") Long userId,
                                               @RequestParam(required = false) CategoryType type) {
        return ApiResponse.ok(categoryService.list(userId, type));
    }

    @PostMapping
    public ApiResponse<CategoryDto> create(@RequestAttribute("userId") Long userId,
                                           @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryDto> update(@RequestAttribute("userId") Long userId,
                                           @PathVariable Long id,
                                           @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        categoryService.delete(userId, id);
        return ApiResponse.ok();
    }
}

package com.ledger.config;

import com.ledger.category.Category;
import com.ledger.category.CategoryRepository;
import com.ledger.category.CategoryType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds system-wide preset categories on first start.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.countByUserIdIsNull() > 0) {
            return;
        }
        List<Category> presets = List.of(
                preset("餐饮", CategoryType.EXPENSE, "🍜", 1),
                preset("交通", CategoryType.EXPENSE, "🚌", 2),
                preset("购物", CategoryType.EXPENSE, "🛒", 3),
                preset("居住", CategoryType.EXPENSE, "🏠", 4),
                preset("娱乐", CategoryType.EXPENSE, "🎮", 5),
                preset("医疗", CategoryType.EXPENSE, "💊", 6),
                preset("教育", CategoryType.EXPENSE, "📚", 7),
                preset("其他支出", CategoryType.EXPENSE, "📦", 8),
                preset("工资", CategoryType.INCOME, "💰", 1),
                preset("奖金", CategoryType.INCOME, "🎁", 2),
                preset("理财", CategoryType.INCOME, "📈", 3),
                preset("兼职", CategoryType.INCOME, "💼", 4),
                preset("其他收入", CategoryType.INCOME, "💵", 5)
        );
        categoryRepository.saveAll(presets);
    }

    private Category preset(String name, CategoryType type, String icon, int sortOrder) {
        return Category.builder()
                .userId(null)
                .name(name)
                .type(type)
                .icon(icon)
                .sortOrder(sortOrder)
                .build();
    }
}

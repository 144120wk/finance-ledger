package com.ledger.stats;

import com.ledger.category.CategoryType;

import java.math.BigDecimal;

public record CategoryTotalDto(Long categoryId, String categoryName, String categoryIcon,
                               CategoryType type, BigDecimal total, BigDecimal percentage) {
}

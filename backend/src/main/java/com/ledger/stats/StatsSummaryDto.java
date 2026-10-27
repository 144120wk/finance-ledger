package com.ledger.stats;

import com.ledger.category.CategoryType;

import java.math.BigDecimal;

public record StatsSummaryDto(BigDecimal income, BigDecimal expense, BigDecimal balance) {
}

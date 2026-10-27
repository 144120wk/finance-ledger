package com.ledger.stats;

import java.math.BigDecimal;

public record TrendPointDto(String month, BigDecimal income, BigDecimal expense, BigDecimal balance) {
}

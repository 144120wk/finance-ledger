package com.ledger.transaction;

import com.ledger.category.CategoryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    private Long categoryId;

    @NotNull(message = "type is required")
    private CategoryType type;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "amount has too many digits")
    private BigDecimal amount;

    @Size(max = 200, message = "note is too long")
    private String note;

    @NotNull(message = "occurredAt is required")
    private LocalDate occurredAt;
}

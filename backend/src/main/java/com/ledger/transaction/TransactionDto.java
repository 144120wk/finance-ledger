package com.ledger.transaction;

import com.ledger.category.CategoryType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDto {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private CategoryType type;
    private BigDecimal amount;
    private String note;
    private LocalDate occurredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TransactionDto from(Transaction t) {
        return TransactionDto.builder()
                .id(t.getId())
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .categoryIcon(t.getCategory() != null ? t.getCategory().getIcon() : null)
                .type(t.getType())
                .amount(t.getAmount())
                .note(t.getNote())
                .occurredAt(t.getOccurredAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}

package com.ledger.transaction;

import com.ledger.category.CategoryType;
import com.ledger.common.ApiException;
import com.ledger.common.ApiResponse;
import com.ledger.common.PageDto;
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

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ApiResponse<PageDto<TransactionDto>> list(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(transactionService.list(
                userId, type, categoryId, parseMonth(month), page, Math.min(size, 100)));
    }

    @PostMapping
    public ApiResponse<TransactionDto> create(@RequestAttribute("userId") Long userId,
                                              @Valid @RequestBody TransactionRequest request) {
        return ApiResponse.ok(transactionService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionDto> update(@RequestAttribute("userId") Long userId,
                                              @PathVariable Long id,
                                              @Valid @RequestBody TransactionRequest request) {
        return ApiResponse.ok(transactionService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        transactionService.delete(userId, id);
        return ApiResponse.ok();
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest("month must use yyyy-MM format, e.g. 2026-10");
        }
    }
}

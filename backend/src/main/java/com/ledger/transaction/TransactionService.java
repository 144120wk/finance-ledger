package com.ledger.transaction;

import com.ledger.category.Category;
import com.ledger.category.CategoryService;
import com.ledger.category.CategoryType;
import com.ledger.common.ApiException;
import com.ledger.common.PageDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public PageDto<TransactionDto> list(Long userId, CategoryType type, Long categoryId,
                                        YearMonth month, int page, int size) {
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (month != null) {
                predicates.add(cb.between(root.get("occurredAt"), month.atDay(1), month.atEndOfMonth()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Transaction> result = transactionRepository.findAll(spec,
                PageRequest.of(page, size,
                        Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"))));

        List<TransactionDto> items = result.getContent().stream().map(TransactionDto::from).toList();
        return new PageDto<>(items, result.getTotalElements(), page, size);
    }

    public TransactionDto create(Long userId, TransactionRequest request) {
        Category category = categoryService.resolveForTransaction(
                userId, request.getCategoryId(), request.getType());
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .category(category)
                .type(request.getType())
                .amount(request.getAmount())
                .note(request.getNote())
                .occurredAt(request.getOccurredAt())
                .build();
        return TransactionDto.from(transactionRepository.save(transaction));
    }

    public TransactionDto update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = getOwnedOrThrow(userId, id);
        Category category = categoryService.resolveForTransaction(
                userId, request.getCategoryId(), request.getType());
        transaction.setCategory(category);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setOccurredAt(request.getOccurredAt());
        return TransactionDto.from(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        transactionRepository.delete(getOwnedOrThrow(userId, id));
    }

    private Transaction getOwnedOrThrow(Long userId, Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Transaction not found"));
        if (!transaction.getUserId().equals(userId)) {
            throw ApiException.notFound("Transaction not found");
        }
        return transaction;
    }
}

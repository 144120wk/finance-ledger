package com.ledger.transaction;

import com.ledger.category.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    // ---- filtered listing ----

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    Page<Transaction> findByUserIdAndType(Long userId, CategoryType type, Pageable pageable);

    Page<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeAndCategoryId(Long userId, CategoryType type, Long categoryId,
                                                       Pageable pageable);

    List<Transaction> findByUserIdAndOccurredAtBetween(Long userId, LocalDate start, LocalDate end);

    // ---- stats ----

    @Query("select coalesce(sum(t.amount), 0) from Transaction t " +
            "where t.userId = :userId and t.type = :type and t.occurredAt between :start and :end")
    BigDecimal sumByTypeInRange(@Param("userId") Long userId,
                                @Param("type") CategoryType type,
                                @Param("start") LocalDate start,
                                @Param("end") LocalDate end);

    // ---- category maintenance ----

    @Modifying
    @Query("update Transaction t set t.category = null where t.category.id = :categoryId")
    int detachCategory(@Param("categoryId") Long categoryId);

    long countByCategoryId(Long categoryId);
}

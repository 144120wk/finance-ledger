package com.ledger.stats;

import com.ledger.category.CategoryType;
import com.ledger.transaction.Transaction;
import com.ledger.transaction.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class StatsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final TransactionRepository transactionRepository;

    public StatsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public StatsSummaryDto summary(Long userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        BigDecimal income = transactionRepository.sumByTypeInRange(userId, CategoryType.INCOME, start, end);
        BigDecimal expense = transactionRepository.sumByTypeInRange(userId, CategoryType.EXPENSE, start, end);
        return new StatsSummaryDto(income, expense, income.subtract(expense));
    }

    @Transactional(readOnly = true)
    public List<CategoryTotalDto> breakdown(Long userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndOccurredAtBetween(userId, start, end);

        Map<String, CategoryTotalDto> groups = new LinkedHashMap<>();
        BigDecimal incomeTotal = ZERO;
        BigDecimal expenseTotal = ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == CategoryType.INCOME) {
                incomeTotal = incomeTotal.add(t.getAmount());
            } else {
                expenseTotal = expenseTotal.add(t.getAmount());
            }
            String key = (t.getCategory() != null ? t.getCategory().getId() : -1L)
                    + ":" + t.getType();
            CategoryTotalDto dto = groups.computeIfAbsent(key, k -> new CategoryTotalDto(
                    t.getCategory() != null ? t.getCategory().getId() : null,
                    t.getCategory() != null ? t.getCategory().getName() : "未分类",
                    t.getCategory() != null ? t.getCategory().getIcon() : null,
                    t.getType(),
                    ZERO,
                    BigDecimal.ZERO));
            groups.put(key, new CategoryTotalDto(
                    dto.categoryId(), dto.categoryName(), dto.categoryIcon(), dto.type(),
                    dto.total().add(t.getAmount()), dto.percentage()));
        }

        final BigDecimal totalIncome = incomeTotal;
        final BigDecimal totalExpense = expenseTotal;
        return groups.values().stream()
                .map(dto -> new CategoryTotalDto(
                        dto.categoryId(), dto.categoryName(), dto.categoryIcon(), dto.type(),
                        dto.total(),
                        percentage(dto.total(), dto.type() == CategoryType.INCOME
                                ? totalIncome : totalExpense)))
                .toList();
    }

    public List<TrendPointDto> trend(Long userId, YearMonth endMonth, int months) {
        YearMonth startMonth = endMonth.minusMonths(Math.max(1, months) - 1L);
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndOccurredAtBetween(userId, startMonth.atDay(1), endMonth.atEndOfMonth());

        Map<YearMonth, BigDecimal> incomeByMonth = new TreeMap<>();
        Map<YearMonth, BigDecimal> expenseByMonth = new TreeMap<>();
        for (Transaction t : transactions) {
            YearMonth ym = YearMonth.from(t.getOccurredAt());
            Map<YearMonth, BigDecimal> target =
                    t.getType() == CategoryType.INCOME ? incomeByMonth : expenseByMonth;
            target.merge(ym, t.getAmount(), BigDecimal::add);
        }

        List<TrendPointDto> result = new ArrayList<>();
        for (YearMonth ym = startMonth; !ym.isAfter(endMonth); ym = ym.plusMonths(1)) {
            BigDecimal income = incomeByMonth.getOrDefault(ym, ZERO);
            BigDecimal expense = expenseByMonth.getOrDefault(ym, ZERO);
            result.add(new TrendPointDto(ym.toString(), income, expense, income.subtract(expense)));
        }
        return result;
    }

    private BigDecimal percentage(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP);
    }
}

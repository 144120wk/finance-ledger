package com.ledger.stats;

import com.ledger.common.ApiException;
import com.ledger.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public ApiResponse<StatsSummaryDto> summary(@RequestAttribute("userId") Long userId,
                                                @RequestParam String month) {
        return ApiResponse.ok(statsService.summary(userId, parseMonth(month)));
    }

    @GetMapping("/breakdown")
    public ApiResponse<List<CategoryTotalDto>> breakdown(@RequestAttribute("userId") Long userId,
                                                         @RequestParam String month) {
        return ApiResponse.ok(statsService.breakdown(userId, parseMonth(month)));
    }

    @GetMapping("/trend")
    public ApiResponse<List<TrendPointDto>> trend(@RequestAttribute("userId") Long userId,
                                                  @RequestParam(defaultValue = "6") int months,
                                                  @RequestParam(required = false) String endMonth) {
        YearMonth end = endMonth == null || endMonth.isBlank()
                ? YearMonth.now()
                : parseMonth(endMonth);
        return ApiResponse.ok(statsService.trend(userId, end, months));
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest("month must use yyyy-MM format, e.g. 2026-10");
        }
    }
}

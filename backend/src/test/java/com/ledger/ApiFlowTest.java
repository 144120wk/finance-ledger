package com.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API smoke test: register -> login -> create transactions -> stats,
 * plus an authorization check.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowTest {

    @Autowired
    private MockMvc mockMvc;

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Test
    void authAndLedgerFlow() throws Exception {
        String username = "alice";
        String password = "secret123";

        // ---- register ----
        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password
                                + "\",\"email\":\"alice@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        String token = extractToken(regResult.getResponse().getContentAsString());
        assertThat(token).isNotBlank();

        // ---- unauthorized without token ----
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());

        // ---- categories include system presets ----
        mockMvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThan(0)));

        // ---- create an expense ----
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"type\":\"EXPENSE\",\"amount\":42.50,"
                                + "\"note\":\"lunch\",\"occurredAt\":\"2026-10-15\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(42.50));

        // ---- create an income ----
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":9,\"type\":\"INCOME\",\"amount\":1000.00,"
                                + "\"note\":\"salary\",\"occurredAt\":\"2026-10-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(1000.00));

        // ---- month summary ----
        mockMvc.perform(get("/api/stats/summary")
                        .param("month", "2026-10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.income").value(1000.00))
                .andExpect(jsonPath("$.data.expense").value(42.50))
                .andExpect(jsonPath("$.data.balance").value(957.50));

        // ---- trend ----
        mockMvc.perform(get("/api/stats/trend")
                        .param("months", "6")
                        .param("endMonth", "2026-10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(6));

        // ---- login returns a usable token ----
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    private String extractToken(String body) {
        Matcher matcher = TOKEN_PATTERN.matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}

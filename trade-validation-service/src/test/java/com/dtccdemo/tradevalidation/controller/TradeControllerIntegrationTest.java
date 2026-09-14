package com.dtccdemo.tradevalidation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests exercising the real Spring context, an in-memory H2
 * database, and the actual REST endpoints — standardize -> validate ->
 * persist -> retrieve, plus the rejected-record audit trail.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TradeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void acceptsAWellFormedTradeAndItIsRetrievable() throws Exception {
        String settlementDate = LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String body = """
                {
                  "sourceSystem": "EQUITIES_CORE",
                  "tradeId": "IT-1001",
                  "counterparty": "Acme Capital",
                  "instrumentId": "US0378331005",
                  "settlementDate": "%s",
                  "amount": "50000.00",
                  "currency": "USD"
                }
                """.formatted(settlementDate);

        mockMvc.perform(post("/trades")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accepted").value(true))
                .andExpect(jsonPath("$.tradeId").value("IT-1001"));

        mockMvc.perform(get("/trades/IT-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counterparty").value("Acme Capital"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void standardizesAliasFieldNamesFromADifferentSourceSystem() throws Exception {
        String body = """
                {
                  "sourceSystem": "DERIVATIVES_LEGACY",
                  "txnRef": "IT-2002",
                  "counterpartyName": "Beta Bank",
                  "securityCode": "SEC-778",
                  "valueDate": "20/12/2026",
                  "amount": "1,250,000.00",
                  "currency": "eur"
                }
                """;

        mockMvc.perform(post("/trades")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tradeId").value("IT-2002"));

        mockMvc.perform(get("/trades/IT-2002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counterparty").value("Beta Bank"))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void rejectsPayloadWithInvalidCurrencyAndRecordsAuditTrail() throws Exception {
        String settlementDate = LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String body = """
                {
                  "sourceSystem": "EQUITIES_CORE",
                  "tradeId": "IT-3003",
                  "counterparty": "Acme Capital",
                  "instrumentId": "US0378331005",
                  "settlementDate": "%s",
                  "amount": "5000.00",
                  "currency": "ZZZ"
                }
                """.formatted(settlementDate);

        mockMvc.perform(post("/trades")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.accepted").value(false))
                .andExpect(jsonPath("$.errors[0]").exists());

        mockMvc.perform(get("/trades/rejected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.rawTradeId == 'IT-3003')]").exists());
    }

    @Test
    void rejectsDuplicateTradeIdOnSecondSubmission() throws Exception {
        String settlementDate = LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String body = """
                {
                  "sourceSystem": "EQUITIES_CORE",
                  "tradeId": "IT-4004",
                  "counterparty": "Acme Capital",
                  "instrumentId": "US0378331005",
                  "settlementDate": "%s",
                  "amount": "5000.00",
                  "currency": "USD"
                }
                """.formatted(settlementDate);

        mockMvc.perform(post("/trades").contentType("application/json").content(body))
                .andExpect(status().isCreated());

        // Submit the exact same tradeId again
        mockMvc.perform(post("/trades").contentType("application/json").content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("duplicate tradeId")));
    }

    @Test
    void rejectsMalformedJsonBodyGracefully() throws Exception {
        mockMvc.perform(post("/trades")
                        .contentType("application/json")
                        .content("{ not valid json"))
                .andExpect(status().isBadRequest());
    }
}

package com.dtccdemo.tradevalidation.service;

import com.dtccdemo.tradevalidation.model.Trade;
import com.dtccdemo.tradevalidation.model.TradeStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradeValidatorTest {

    private final TradeValidator validator = new TradeValidator();

    private Trade validTrade() {
        Trade trade = new Trade();
        trade.setTradeId("T-1");
        trade.setCounterparty("Acme Capital");
        trade.setInstrumentId("US0378331005");
        trade.setSettlementDate(LocalDate.now().plusDays(2));
        trade.setAmount(new BigDecimal("1000.00"));
        trade.setCurrency("USD");
        trade.setStatus(TradeStatus.ACCEPTED);
        return trade;
    }

    @Test
    void acceptsAWellFormedTrade() {
        List<String> errors = validator.validate(validTrade(), false);
        assertTrue(errors.isEmpty());
    }

    @Test
    void rejectsMissingTradeId() {
        Trade trade = validTrade();
        trade.setTradeId(null);

        List<String> errors = validator.validate(trade, false);

        assertTrue(errors.stream().anyMatch(e -> e.contains("tradeId is required")));
    }

    @Test
    void rejectsDuplicateTradeId() {
        Trade trade = validTrade();

        List<String> errors = validator.validate(trade, true);

        assertTrue(errors.stream().anyMatch(e -> e.contains("duplicate tradeId")));
    }

    @Test
    void rejectsMissingCounterparty() {
        Trade trade = validTrade();
        trade.setCounterparty("  ");

        List<String> errors = validator.validate(trade, false);

        assertTrue(errors.stream().anyMatch(e -> e.contains("counterparty is required")));
    }

    @Test
    void rejectsUnparsedSettlementDate() {
        Trade trade = validTrade();
        trade.setSettlementDate(null);

        List<String> errors = validator.validate(trade, false);

        assertTrue(errors.stream().anyMatch(e -> e.contains("settlementDate is required")));
    }

    @Test
    void rejectsSettlementDateTooFarInFuture() {
        Trade trade = validTrade();
        trade.setSettlementDate(LocalDate.now().plusDays(90));

        List<String> errors = validator.validate(trade, false);

        assertTrue(errors.stream().anyMatch(e -> e.contains("too far in the future")));
    }

    @Test
    void rejectsSettlementDateTooFarInPast() {
        Trade trade = validTrade();
        trade.setSettlementDate(LocalDate.now().minusDays(30));

        List<String> errors = validator.validate(trade, false);

        assertTrue(errors.stream().anyMatch(e -> e.contains("too far in the past")));
    }

    @Test
    void rejectsZeroOrNegativeAmount() {
        Trade trade = validTrade();
        trade.setAmount(BigDecimal.ZERO);

        List<String> errors = validator.validate(trade, false);

        assertTrue(errors.stream().anyMatch(e -> e.contains("amount must be greater than zero")));
    }

    @Test
    void rejectsAmountAboveThreshold() {
        Trade trade = validTrade();
        trade.setAmount(new BigDecimal("9999999999"));

        List<String> errors = validator.validate(trade, false);

        assertTrue(errors.stream().anyMatch(e -> e.contains("exceeds maximum allowed threshold")));
    }

    @Test
    void rejectsUnrecognizedCurrencyCode() {
        Trade trade = validTrade();
        trade.setCurrency("XYZ");

        List<String> errors = validator.validate(trade, false);

        assertTrue(errors.stream().anyMatch(e -> e.contains("not a recognized ISO 4217 code")));
    }

    @Test
    void accumulatesMultipleErrorsAtOnce() {
        Trade trade = new Trade();
        trade.setStatus(TradeStatus.ACCEPTED);
        // everything else left null/blank

        List<String> errors = validator.validate(trade, false);

        // tradeId, counterparty, instrumentId, settlementDate, amount, currency
        assertEquals(6, errors.size());
    }
}

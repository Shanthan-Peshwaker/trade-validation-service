package com.dtccdemo.tradevalidation.service;

import com.dtccdemo.tradevalidation.model.Trade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Data-governance / quality rules applied to every standardized trade before
 * it is allowed to persist. Centralizing these rules (rather than scattering
 * checks across callers) makes the governance policy explicit, testable, and
 * easy to extend as new rules are needed.
 */
@Component
public class TradeValidator {

    // ISO 4217-style currency allow-list kept small and explicit for this
    // demo; in production this would be backed by a maintained reference
    // data table.
    private static final Set<String> VALID_CURRENCIES = Set.of(
            "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "INR"
    );

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000"); // 1 billion sanity ceiling

    private static final int MAX_SETTLEMENT_DAYS_IN_FUTURE = 30;
    private static final int MAX_SETTLEMENT_DAYS_IN_PAST = 3;

    public List<String> validate(Trade trade, boolean tradeIdAlreadyExists) {
        List<String> errors = new ArrayList<>();

        if (isBlank(trade.getTradeId())) {
            errors.add("tradeId is required");
        } else if (tradeIdAlreadyExists) {
            errors.add("duplicate tradeId: " + trade.getTradeId());
        }

        if (isBlank(trade.getCounterparty())) {
            errors.add("counterparty is required");
        }

        if (isBlank(trade.getInstrumentId())) {
            errors.add("instrumentId is required");
        }

        if (trade.getSettlementDate() == null) {
            errors.add("settlementDate is required or could not be parsed");
        } else {
            validateSettlementDate(trade.getSettlementDate(), errors);
        }

        if (trade.getAmount() == null) {
            errors.add("amount is required or could not be parsed");
        } else {
            validateAmount(trade.getAmount(), errors);
        }

        if (isBlank(trade.getCurrency())) {
            errors.add("currency is required");
        } else if (!VALID_CURRENCIES.contains(trade.getCurrency().toUpperCase())) {
            errors.add("currency is not a recognized ISO 4217 code: " + trade.getCurrency());
        }

        return errors;
    }

    private void validateSettlementDate(LocalDate settlementDate, List<String> errors) {
        LocalDate today = LocalDate.now();
        LocalDate earliest = today.minusDays(MAX_SETTLEMENT_DAYS_IN_PAST);
        LocalDate latest = today.plusDays(MAX_SETTLEMENT_DAYS_IN_FUTURE);

        if (settlementDate.isBefore(earliest)) {
            errors.add("settlementDate is too far in the past: " + settlementDate);
        }
        if (settlementDate.isAfter(latest)) {
            errors.add("settlementDate is too far in the future: " + settlementDate);
        }
    }

    private void validateAmount(BigDecimal amount, List<String> errors) {
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            errors.add("amount must be greater than zero");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            errors.add("amount exceeds maximum allowed threshold");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

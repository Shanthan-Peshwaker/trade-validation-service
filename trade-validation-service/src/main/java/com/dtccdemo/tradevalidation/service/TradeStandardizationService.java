package com.dtccdemo.tradevalidation.service;

import com.dtccdemo.tradevalidation.dto.IncomingTradeRequest;
import com.dtccdemo.tradevalidation.model.Trade;
import com.dtccdemo.tradevalidation.model.TradeStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Maps heterogeneous inbound payloads (different field names/date formats
 * depending on source system) into the canonical {@link Trade} schema.
 * <p>
 * This is the "standardization" half of the ingest/validate/standardize
 * pipeline: it never rejects data itself, it just normalizes whatever it
 * can and leaves nulls where a field couldn't be parsed. Rejection is the
 * validator's job, so parsing concerns and business-rule concerns stay
 * separate and independently testable.
 */
@Service
public class TradeStandardizationService {

    private static final List<DateTimeFormatter> SUPPORTED_DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,              // 2026-09-14
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),      // 14/09/2026
            DateTimeFormatter.ofPattern("MM-dd-yyyy")       // 09-14-2026
    );

    public Trade standardize(IncomingTradeRequest request) {
        Trade trade = new Trade();
        trade.setSourceSystem(nullSafeTrim(request.getSourceSystem()));
        trade.setTradeId(firstNonBlank(request.getTradeId(), request.getTxnRef()));
        trade.setCounterparty(firstNonBlank(request.getCounterparty(), request.getCounterpartyName()));
        trade.setInstrumentId(firstNonBlank(request.getInstrumentId(), request.getSecurityCode()));
        trade.setSettlementDate(parseDate(firstNonBlank(request.getSettlementDate(), request.getValueDate())));
        trade.setAmount(parseAmount(request.getAmount()));
        trade.setCurrency(request.getCurrency() == null ? null : request.getCurrency().trim().toUpperCase());
        trade.setStatus(TradeStatus.ACCEPTED); // provisional; validator/caller may override to REJECTED
        return trade;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        for (DateTimeFormatter formatter : SUPPORTED_DATE_FORMATS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            // Defensively strip common formatting noise like thousands separators
            String cleaned = raw.trim().replace(",", "");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a.trim();
        }
        if (b != null && !b.isBlank()) {
            return b.trim();
        }
        return null;
    }

    private String nullSafeTrim(String s) {
        return s == null ? null : s.trim();
    }
}

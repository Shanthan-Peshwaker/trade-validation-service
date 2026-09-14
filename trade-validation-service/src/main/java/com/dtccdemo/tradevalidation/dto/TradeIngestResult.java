package com.dtccdemo.tradevalidation.dto;

import java.util.List;

public class TradeIngestResult {

    private final boolean accepted;
    private final String tradeId;
    private final List<String> errors;

    private TradeIngestResult(boolean accepted, String tradeId, List<String> errors) {
        this.accepted = accepted;
        this.tradeId = tradeId;
        this.errors = errors;
    }

    public static TradeIngestResult accepted(String tradeId) {
        return new TradeIngestResult(true, tradeId, List.of());
    }

    public static TradeIngestResult rejected(List<String> errors) {
        return new TradeIngestResult(false, null, errors);
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getTradeId() {
        return tradeId;
    }

    public List<String> getErrors() {
        return errors;
    }
}

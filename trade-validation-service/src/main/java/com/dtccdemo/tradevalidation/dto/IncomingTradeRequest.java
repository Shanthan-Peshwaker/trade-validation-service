package com.dtccdemo.tradevalidation.dto;

/**
 * Represents a raw trade payload as it might arrive from one of several
 * upstream "source systems". In the real world, different systems send
 * different field names for conceptually the same data (e.g. one system
 * calls it "tradeId", another "txnRef"). Rather than force every client to
 * match one schema up front, this DTO accepts both aliases per field, and
 * the {@link com.dtccdemo.tradevalidation.service.TradeStandardizationService}
 * picks whichever is populated.
 *
 * This mirrors the real-world integration problem DTCC-style platforms
 * solve: normalize heterogeneous inbound formats into one canonical model.
 */
public class IncomingTradeRequest {

    private String sourceSystem;

    // Trade identifier aliases
    private String tradeId;
    private String txnRef;

    // Counterparty aliases
    private String counterparty;
    private String counterpartyName;

    // Instrument aliases
    private String instrumentId;
    private String securityCode;

    // Settlement date aliases (kept as String so we can validate/parse
    // multiple incoming date formats explicitly, rather than failing at
    // JSON deserialization time with an opaque error)
    private String settlementDate;
    private String valueDate;

    private String amount;
    private String currency;

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getTxnRef() {
        return txnRef;
    }

    public void setTxnRef(String txnRef) {
        this.txnRef = txnRef;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(String counterparty) {
        this.counterparty = counterparty;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public void setCounterpartyName(String counterpartyName) {
        this.counterpartyName = counterpartyName;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public String getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(String settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

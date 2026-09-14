package com.dtccdemo.tradevalidation.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Standardized trade record.
 * <p>
 * This is the common schema that all incoming source-system payloads
 * (which may use different field names/formats) are mapped into. Loosely
 * inspired by ISO 20022 concepts (counterparty, instrument, settlement date,
 * amount, currency) used broadly across financial market infrastructure.
 */
@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @Column(name = "trade_id", nullable = false, updatable = false)
    private String tradeId;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "counterparty", nullable = false)
    private String counterparty;

    @Column(name = "instrument_id", nullable = false)
    private String instrumentId;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TradeStatus status;

    public Trade() {
    }

    public Trade(String tradeId, String sourceSystem, String counterparty, String instrumentId,
                 LocalDate settlementDate, BigDecimal amount, String currency, TradeStatus status) {
        this.tradeId = tradeId;
        this.sourceSystem = sourceSystem;
        this.counterparty = counterparty;
        this.instrumentId = instrumentId;
        this.settlementDate = settlementDate;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(String counterparty) {
        this.counterparty = counterparty;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }
}

package com.dtccdemo.tradevalidation.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Audit record for a trade payload that failed governance/validation rules.
 * Keeping a rejected-record trail is a common data-governance pattern: every
 * inbound record is accounted for, whether it was accepted or not, and why.
 */
@Entity
@Table(name = "rejected_trades")
public class RejectedTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_system")
    private String sourceSystem;

    @Column(name = "raw_trade_id")
    private String rawTradeId;

    @Lob
    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "rejected_at", nullable = false)
    private Instant rejectedAt;

    public RejectedTrade() {
    }

    public RejectedTrade(String sourceSystem, String rawTradeId, String reason, Instant rejectedAt) {
        this.sourceSystem = sourceSystem;
        this.rawTradeId = rawTradeId;
        this.reason = reason;
        this.rejectedAt = rejectedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getRawTradeId() {
        return rawTradeId;
    }

    public void setRawTradeId(String rawTradeId) {
        this.rawTradeId = rawTradeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
}

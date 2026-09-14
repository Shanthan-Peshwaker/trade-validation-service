package com.dtccdemo.tradevalidation.service;

import com.dtccdemo.tradevalidation.dto.IncomingTradeRequest;
import com.dtccdemo.tradevalidation.model.Trade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TradeStandardizationServiceTest {

    private final TradeStandardizationService service = new TradeStandardizationService();

    @Test
    void standardizesPrimaryFieldNames() {
        IncomingTradeRequest req = new IncomingTradeRequest();
        req.setSourceSystem("EQUITIES_CORE");
        req.setTradeId("T-1001");
        req.setCounterparty("Acme Capital");
        req.setInstrumentId("US0378331005");
        req.setSettlementDate("2026-09-20");
        req.setAmount("15000.50");
        req.setCurrency("usd");

        Trade trade = service.standardize(req);

        assertEquals("T-1001", trade.getTradeId());
        assertEquals("Acme Capital", trade.getCounterparty());
        assertEquals("US0378331005", trade.getInstrumentId());
        assertEquals(LocalDate.of(2026, 9, 20), trade.getSettlementDate());
        assertEquals(0, new BigDecimal("15000.50").compareTo(trade.getAmount()));
        assertEquals("USD", trade.getCurrency());
    }

    @Test
    void fallsBackToAliasFieldNamesWhenPrimaryFieldsAreMissing() {
        // Simulates a different upstream system that uses alternate field names
        IncomingTradeRequest req = new IncomingTradeRequest();
        req.setSourceSystem("DERIVATIVES_LEGACY");
        req.setTxnRef("TXN-9988");
        req.setCounterpartyName("Beta Bank");
        req.setSecurityCode("SEC-778");
        req.setValueDate("20/09/2026"); // dd/MM/yyyy format
        req.setAmount("2,500,000.00"); // thousands separators
        req.setCurrency("EUR");

        Trade trade = service.standardize(req);

        assertEquals("TXN-9988", trade.getTradeId());
        assertEquals("Beta Bank", trade.getCounterparty());
        assertEquals("SEC-778", trade.getInstrumentId());
        assertEquals(LocalDate.of(2026, 9, 20), trade.getSettlementDate());
        assertEquals(0, new BigDecimal("2500000.00").compareTo(trade.getAmount()));
        assertEquals("EUR", trade.getCurrency());
    }

    @Test
    void supportsAlternateDateFormat_MMddYYYY() {
        IncomingTradeRequest req = new IncomingTradeRequest();
        req.setTradeId("T-2002");
        req.setSettlementDate("09-20-2026");

        Trade trade = service.standardize(req);

        assertEquals(LocalDate.of(2026, 9, 20), trade.getSettlementDate());
    }

    @Test
    void leavesDateNullWhenUnparseable() {
        IncomingTradeRequest req = new IncomingTradeRequest();
        req.setTradeId("T-3003");
        req.setSettlementDate("not-a-date");

        Trade trade = service.standardize(req);

        assertNull(trade.getSettlementDate());
    }

    @Test
    void leavesAmountNullWhenUnparseable() {
        IncomingTradeRequest req = new IncomingTradeRequest();
        req.setTradeId("T-4004");
        req.setAmount("abc");

        Trade trade = service.standardize(req);

        assertNull(trade.getAmount());
    }

    @Test
    void leavesTradeIdNullWhenNeitherAliasProvided() {
        IncomingTradeRequest req = new IncomingTradeRequest();
        req.setCounterparty("Some Counterparty");

        Trade trade = service.standardize(req);

        assertNull(trade.getTradeId());
    }
}

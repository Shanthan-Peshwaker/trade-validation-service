package com.dtccdemo.tradevalidation.service;

import com.dtccdemo.tradevalidation.dto.IncomingTradeRequest;
import com.dtccdemo.tradevalidation.dto.TradeIngestResult;
import com.dtccdemo.tradevalidation.model.RejectedTrade;
import com.dtccdemo.tradevalidation.model.Trade;
import com.dtccdemo.tradevalidation.model.TradeStatus;
import com.dtccdemo.tradevalidation.repository.RejectedTradeRepository;
import com.dtccdemo.tradevalidation.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Orchestrates the full ingest pipeline for a single incoming trade payload:
 * standardize -&gt; validate -&gt; persist (either as an accepted Trade, or as a
 * RejectedTrade audit record with the reasons attached).
 * <p>
 * Every inbound record is accounted for either way, which is the core
 * governance property this service is meant to demonstrate: nothing is
 * silently dropped.
 */
@Service
public class TradeIngestService {

    private final TradeStandardizationService standardizationService;
    private final TradeValidator validator;
    private final TradeRepository tradeRepository;
    private final RejectedTradeRepository rejectedTradeRepository;

    public TradeIngestService(TradeStandardizationService standardizationService,
                               TradeValidator validator,
                               TradeRepository tradeRepository,
                               RejectedTradeRepository rejectedTradeRepository) {
        this.standardizationService = standardizationService;
        this.validator = validator;
        this.tradeRepository = tradeRepository;
        this.rejectedTradeRepository = rejectedTradeRepository;
    }

    @Transactional
    public TradeIngestResult ingest(IncomingTradeRequest request) {
        Trade trade = standardizationService.standardize(request);

        boolean duplicateId = trade.getTradeId() != null && tradeRepository.existsById(trade.getTradeId());
        List<String> errors = validator.validate(trade, duplicateId);

        if (!errors.isEmpty()) {
            String rawId = trade.getTradeId() != null ? trade.getTradeId() : "(unparsed)";
            RejectedTrade rejected = new RejectedTrade(
                    request.getSourceSystem(),
                    rawId,
                    String.join("; ", errors),
                    Instant.now()
            );
            rejectedTradeRepository.save(rejected);
            return TradeIngestResult.rejected(errors);
        }

        trade.setStatus(TradeStatus.ACCEPTED);
        tradeRepository.save(trade);
        return TradeIngestResult.accepted(trade.getTradeId());
    }
}

package com.dtccdemo.tradevalidation.controller;

import com.dtccdemo.tradevalidation.dto.IncomingTradeRequest;
import com.dtccdemo.tradevalidation.dto.TradeIngestResult;
import com.dtccdemo.tradevalidation.model.RejectedTrade;
import com.dtccdemo.tradevalidation.model.Trade;
import com.dtccdemo.tradevalidation.repository.RejectedTradeRepository;
import com.dtccdemo.tradevalidation.repository.TradeRepository;
import com.dtccdemo.tradevalidation.service.TradeIngestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trades")
public class TradeController {

    private final TradeIngestService ingestService;
    private final TradeRepository tradeRepository;
    private final RejectedTradeRepository rejectedTradeRepository;

    public TradeController(TradeIngestService ingestService,
                            TradeRepository tradeRepository,
                            RejectedTradeRepository rejectedTradeRepository) {
        this.ingestService = ingestService;
        this.tradeRepository = tradeRepository;
        this.rejectedTradeRepository = rejectedTradeRepository;
    }

    /**
     * Accepts a raw trade payload (in whatever shape a given source system
     * sends), standardizes it, validates it against governance rules, and
     * persists it. Returns 201 on success, 422 with the list of validation
     * errors when the payload is rejected.
     */
    @PostMapping
    public ResponseEntity<TradeIngestResult> ingest(@RequestBody IncomingTradeRequest request) {
        TradeIngestResult result = ingestService.ingest(request);
        if (result.isAccepted()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        return ResponseEntity.unprocessableEntity().body(result);
    }

    @GetMapping
    public List<Trade> listAccepted() {
        return tradeRepository.findAll();
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<Trade> getOne(@PathVariable String tradeId) {
        return tradeRepository.findById(tradeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/rejected")
    public List<RejectedTrade> listRejected() {
        return rejectedTradeRepository.findAll();
    }
}

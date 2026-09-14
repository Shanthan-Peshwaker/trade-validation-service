package com.dtccdemo.tradevalidation.repository;

import com.dtccdemo.tradevalidation.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, String> {
}

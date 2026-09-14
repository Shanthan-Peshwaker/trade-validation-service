package com.dtccdemo.tradevalidation.repository;

import com.dtccdemo.tradevalidation.model.RejectedTrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectedTradeRepository extends JpaRepository<RejectedTrade, Long> {
}

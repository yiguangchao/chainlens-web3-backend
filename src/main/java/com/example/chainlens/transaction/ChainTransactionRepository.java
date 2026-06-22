package com.example.chainlens.transaction;

import com.example.chainlens.risk.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChainTransactionRepository extends JpaRepository<ChainTransaction, Long> {
    Optional<ChainTransaction> findByTxHashIgnoreCase(String txHash);

    boolean existsByTxHashIgnoreCaseAndLogIndex(String txHash, Long logIndex);

    Page<ChainTransaction> findByChainId(Long chainId, Pageable pageable);

    Page<ChainTransaction> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);

    @Query("""
            select tx from ChainTransaction tx
            where lower(tx.fromAddress) = lower(:address) or lower(tx.toAddress) = lower(:address)
            """)
    Page<ChainTransaction> findByWalletAddress(@Param("address") String address, Pageable pageable);

    long countByFromAddressIgnoreCaseAndTxTimeAfter(String fromAddress, LocalDateTime after);
}

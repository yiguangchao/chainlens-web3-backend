package com.example.chainlens.transaction;

import com.example.chainlens.risk.RiskLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chain_transaction", uniqueConstraints = @UniqueConstraint(name = "uk_tx_hash_log_index", columnNames = {"tx_hash", "log_index"}))
public class ChainTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_id", nullable = false)
    private Long chainId;

    @Column(name = "tx_hash", nullable = false)
    private String txHash;

    @Column(name = "block_number", nullable = false)
    private Long blockNumber;

    @Column(name = "log_index", nullable = false)
    private Long logIndex;

    @Column(name = "from_address")
    private String fromAddress;

    @Column(name = "to_address")
    private String toAddress;

    @Column(name = "token_address")
    private String tokenAddress;

    @Column(name = "token_symbol")
    private String tokenSymbol;

    private BigDecimal amount;

    @Column(name = "native_token", nullable = false)
    private boolean nativeToken;

    @Column(name = "gas_used")
    private Long gasUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_status", nullable = false)
    private TxStatus txStatus;

    @Column(name = "tx_time", nullable = false)
    private LocalDateTime txTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "risk_reason", length = 1000)
    private String riskReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.fromAddress = normalize(fromAddress);
        this.toAddress = normalize(toAddress);
        this.tokenAddress = normalize(tokenAddress);
    }

    public static String normalize(String address) {
        return address == null ? null : address.toLowerCase();
    }
}

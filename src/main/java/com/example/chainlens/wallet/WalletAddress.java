package com.example.chainlens.wallet;

import com.example.chainlens.risk.RiskLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wallet_address", uniqueConstraints = @UniqueConstraint(name = "uk_wallet_chain_address", columnNames = {"chain_id", "address"}))
public class WalletAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_id", nullable = false)
    private Long chainId;

    @Column(nullable = false, length = 80)
    private String address;

    private String label;
    private String remark;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.riskLevel == null) {
            this.riskLevel = RiskLevel.LOW;
        }
        this.address = normalize(address);
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.address = normalize(address);
    }

    public static String normalize(String address) {
        return address == null ? null : address.toLowerCase();
    }
}

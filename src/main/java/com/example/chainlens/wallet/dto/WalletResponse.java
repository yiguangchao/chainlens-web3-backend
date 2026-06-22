package com.example.chainlens.wallet.dto;

import com.example.chainlens.risk.RiskLevel;
import com.example.chainlens.wallet.WalletAddress;

import java.time.LocalDateTime;

public record WalletResponse(
        Long id,
        Long chainId,
        String address,
        String label,
        String remark,
        RiskLevel riskLevel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WalletResponse from(WalletAddress wallet) {
        return new WalletResponse(wallet.getId(), wallet.getChainId(), wallet.getAddress(), wallet.getLabel(),
                wallet.getRemark(), wallet.getRiskLevel(), wallet.getCreatedAt(), wallet.getUpdatedAt());
    }
}

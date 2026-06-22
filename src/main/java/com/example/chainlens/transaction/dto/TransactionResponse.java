package com.example.chainlens.transaction.dto;

import com.example.chainlens.risk.RiskLevel;
import com.example.chainlens.transaction.ChainTransaction;
import com.example.chainlens.transaction.TxStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long chainId,
        String txHash,
        Long blockNumber,
        Long logIndex,
        String fromAddress,
        String toAddress,
        String tokenAddress,
        String tokenSymbol,
        BigDecimal amount,
        boolean nativeToken,
        Long gasUsed,
        TxStatus txStatus,
        LocalDateTime txTime,
        RiskLevel riskLevel,
        String riskReason,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(ChainTransaction tx) {
        return new TransactionResponse(tx.getId(), tx.getChainId(), tx.getTxHash(), tx.getBlockNumber(), tx.getLogIndex(),
                tx.getFromAddress(), tx.getToAddress(), tx.getTokenAddress(), tx.getTokenSymbol(), tx.getAmount(),
                tx.isNativeToken(), tx.getGasUsed(), tx.getTxStatus(), tx.getTxTime(), tx.getRiskLevel(),
                tx.getRiskReason(), tx.getCreatedAt());
    }
}

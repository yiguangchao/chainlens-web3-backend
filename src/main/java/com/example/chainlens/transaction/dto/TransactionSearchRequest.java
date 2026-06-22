package com.example.chainlens.transaction.dto;

import com.example.chainlens.risk.RiskLevel;

public record TransactionSearchRequest(Long chainId, RiskLevel riskLevel) {
}

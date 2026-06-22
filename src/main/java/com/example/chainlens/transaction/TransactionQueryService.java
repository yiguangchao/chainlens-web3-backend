package com.example.chainlens.transaction;

import com.example.chainlens.common.NotFoundException;
import com.example.chainlens.risk.RiskLevel;
import com.example.chainlens.transaction.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {
    private final ChainTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public TransactionResponse getByTxHash(String txHash) {
        return transactionRepository.findByTxHashIgnoreCase(txHash)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new NotFoundException("transaction not found"));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> search(Long chainId, RiskLevel riskLevel, Pageable pageable) {
        Page<ChainTransaction> page;
        if (chainId != null) {
            page = transactionRepository.findByChainId(chainId, pageable);
        } else if (riskLevel != null) {
            page = transactionRepository.findByRiskLevel(riskLevel, pageable);
        } else {
            page = transactionRepository.findAll(pageable);
        }
        return page.map(TransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getByAddress(String address, Pageable pageable) {
        return transactionRepository.findByWalletAddress(address, pageable).map(TransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getByRisk(RiskLevel riskLevel, Pageable pageable) {
        return transactionRepository.findByRiskLevel(riskLevel, pageable).map(TransactionResponse::from);
    }
}

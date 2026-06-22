package com.example.chainlens.transaction;

import com.example.chainlens.risk.RiskLevel;
import com.example.chainlens.transaction.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionQueryService transactionQueryService;

    @GetMapping("/{txHash}")
    public TransactionResponse getByTxHash(@PathVariable String txHash) {
        return transactionQueryService.getByTxHash(txHash);
    }

    @GetMapping
    public Page<TransactionResponse> search(@RequestParam(required = false) Long chainId,
                                            @RequestParam(required = false) RiskLevel riskLevel,
                                            Pageable pageable) {
        return transactionQueryService.search(chainId, riskLevel, pageable);
    }

    @GetMapping("/address/{address}")
    public Page<TransactionResponse> getByAddress(@PathVariable String address, Pageable pageable) {
        return transactionQueryService.getByAddress(address, pageable);
    }

    @GetMapping("/risk/{riskLevel}")
    public Page<TransactionResponse> getByRisk(@PathVariable RiskLevel riskLevel, Pageable pageable) {
        return transactionQueryService.getByRisk(riskLevel, pageable);
    }
}

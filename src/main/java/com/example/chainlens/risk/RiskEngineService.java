package com.example.chainlens.risk;

import com.example.chainlens.transaction.ChainTransaction;
import com.example.chainlens.transaction.ChainTransactionRepository;
import com.example.chainlens.transaction.TxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskEngineService {
    private static final BigDecimal MEDIUM_AMOUNT = new BigDecimal("10000");
    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("100000");
    private static final long HIGH_GAS = 200_000L;

    private final RiskBlacklistRepository blacklistRepository;
    private final ChainTransactionRepository transactionRepository;

    public RiskAssessment assess(ChainTransaction tx) {
        RiskLevel level = RiskLevel.LOW;
        List<String> reasons = new ArrayList<>();

        if (tx.getAmount() != null && tx.getAmount().compareTo(HIGH_AMOUNT) > 0) {
            level = max(level, RiskLevel.HIGH);
            reasons.add("single transfer amount greater than 100000");
        } else if (tx.getAmount() != null && tx.getAmount().compareTo(MEDIUM_AMOUNT) > 0) {
            level = max(level, RiskLevel.MEDIUM);
            reasons.add("single transfer amount greater than 10000");
        }

        if (tx.getToAddress() != null) {
            blacklistRepository.findByAddressIgnoreCase(tx.getToAddress()).ifPresent(item ->
                    reasons.add("toAddress hit blacklist: " + item.getReason()));
            if (reasons.stream().anyMatch(reason -> reason.startsWith("toAddress hit blacklist"))) {
                level = max(level, RiskLevel.HIGH);
            }
        }

        if (tx.getFromAddress() != null) {
            long recentCount = transactionRepository.countByFromAddressIgnoreCaseAndTxTimeAfter(
                    tx.getFromAddress(), LocalDateTime.now().minusMinutes(10));
            if (recentCount >= 10) {
                level = max(level, RiskLevel.MEDIUM);
                reasons.add("same fromAddress transferred more than 10 times in a short window");
            }
        }

        if (tx.getGasUsed() != null && tx.getGasUsed() > HIGH_GAS) {
            level = max(level, RiskLevel.MEDIUM);
            reasons.add("gasUsed is unusually high");
        }

        if (tx.getTxStatus() == TxStatus.FAILED) {
            level = max(level, RiskLevel.LOW);
            reasons.add("contract call failed");
        }

        if (reasons.isEmpty()) {
            reasons.add("no elevated risk rule matched");
        }
        return new RiskAssessment(level, String.join("; ", reasons));
    }

    private RiskLevel max(RiskLevel left, RiskLevel right) {
        return left.ordinal() >= right.ordinal() ? left : right;
    }
}

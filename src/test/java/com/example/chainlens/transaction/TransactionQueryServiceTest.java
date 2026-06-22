package com.example.chainlens.transaction;

import com.example.chainlens.risk.RiskLevel;
import com.example.chainlens.transaction.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TransactionQueryServiceTest {

    @Autowired
    private TransactionQueryService transactionQueryService;

    @Test
    void getByTxHash() {
        TransactionResponse tx = transactionQueryService.getByTxHash("0xmock000000000000000000000000000000000000000000000000000000000001");

        assertThat(tx.chainId()).isEqualTo(11155111L);
        assertThat(tx.nativeToken()).isTrue();
    }

    @Test
    void getByRiskLevel() {
        Page<TransactionResponse> page = transactionQueryService.getByRisk(RiskLevel.HIGH, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent()).allMatch(tx -> tx.riskLevel() == RiskLevel.HIGH);
    }
}

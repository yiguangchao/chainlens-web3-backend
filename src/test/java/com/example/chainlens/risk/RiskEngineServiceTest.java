package com.example.chainlens.risk;

import com.example.chainlens.transaction.ChainTransaction;
import com.example.chainlens.transaction.TxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RiskEngineServiceTest {

    @Autowired
    private RiskEngineService riskEngineService;

    @Test
    void markHighWhenToAddressInBlacklist() {
        ChainTransaction tx = sample();
        tx.setToAddress("0x9999999999999999999999999999999999999999");

        RiskAssessment assessment = riskEngineService.assess(tx);

        assertThat(assessment.riskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(assessment.riskReason()).contains("hit blacklist");
    }

    @Test
    void markMediumWhenAmountGreaterThan10000() {
        ChainTransaction tx = sample();
        tx.setAmount(new BigDecimal("15000"));

        RiskAssessment assessment = riskEngineService.assess(tx);

        assertThat(assessment.riskLevel()).isEqualTo(RiskLevel.MEDIUM);
        assertThat(assessment.riskReason()).contains("10000");
    }

    private ChainTransaction sample() {
        ChainTransaction tx = new ChainTransaction();
        tx.setChainId(11155111L);
        tx.setTxHash("0xtest");
        tx.setBlockNumber(1L);
        tx.setLogIndex(0L);
        tx.setFromAddress("0x1111111111111111111111111111111111111111");
        tx.setToAddress("0x2222222222222222222222222222222222222222");
        tx.setAmount(new BigDecimal("1"));
        tx.setGasUsed(21000L);
        tx.setTxStatus(TxStatus.SUCCESS);
        tx.setTxTime(LocalDateTime.now());
        return tx;
    }
}

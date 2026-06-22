package com.example.chainlens.indexer;

import com.example.chainlens.chain.ChainLensProperties;
import com.example.chainlens.indexer.dto.SyncResult;
import com.example.chainlens.risk.RiskAssessment;
import com.example.chainlens.risk.RiskEngineService;
import com.example.chainlens.transaction.ChainTransaction;
import com.example.chainlens.transaction.ChainTransactionRepository;
import com.example.chainlens.transaction.TxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionIndexerService {
    private final ChainLensProperties properties;
    private final ChainTransactionRepository transactionRepository;
    private final RiskEngineService riskEngineService;

    @Transactional
    public SyncResult mockSync() {
        long fromBlock = 9_000_000L;
        long toBlock = fromBlock + 9;
        int saved = 0;
        for (int i = 0; i < 10; i++) {
            ChainTransaction tx = mockTransaction(i, fromBlock + i);
            saved += saveIfAbsent(tx);
        }
        return new SyncResult(11155111L, fromBlock, toBlock, saved, "mock");
    }

    @Transactional
    public SyncResult syncRange(Long chainId, Long fromBlock, Long toBlock) {
        if (fromBlock > toBlock) {
            throw new IllegalArgumentException("fromBlock must be less than or equal to toBlock");
        }
        if ("rpc".equalsIgnoreCase(properties.getIndexer().getMode())) {
            return rpcSync(chainId, fromBlock, toBlock);
        }
        int saved = 0;
        for (long block = fromBlock; block <= toBlock; block++) {
            ChainTransaction tx = mockTransaction((int) (block - fromBlock), block);
            tx.setChainId(chainId);
            saved += saveIfAbsent(tx);
        }
        return new SyncResult(chainId, fromBlock, toBlock, saved, "mock");
    }

    public BigInteger latestBlock(Long chainId) {
        ChainLensProperties.ChainItem chain = findChain(chainId);
        try {
            return Web3j.build(new HttpService(chain.getRpcUrl())).ethBlockNumber().send().getBlockNumber();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to fetch latest block: " + exception.getMessage(), exception);
        }
    }

    private SyncResult rpcSync(Long chainId, Long fromBlock, Long toBlock) {
        ChainLensProperties.ChainItem chain = findChain(chainId);
        Web3j web3j = Web3j.build(new HttpService(chain.getRpcUrl()));
        int saved = 0;
        for (long blockNumber = fromBlock; blockNumber <= toBlock; blockNumber++) {
            try {
                EthBlock block = web3j.ethGetBlockByNumber(org.web3j.protocol.core.DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), true).send();
                if (block.getBlock() == null) {
                    continue;
                }
                for (EthBlock.TransactionResult<?> result : block.getBlock().getTransactions()) {
                    EthBlock.TransactionObject object = (EthBlock.TransactionObject) result.get();
                    ChainTransaction tx = new ChainTransaction();
                    tx.setChainId(chainId);
                    tx.setTxHash(object.getHash());
                    tx.setBlockNumber(blockNumber);
                    tx.setLogIndex(0L);
                    tx.setFromAddress(object.getFrom());
                    tx.setToAddress(object.getTo());
                    tx.setTokenSymbol(chain.getNativeSymbol());
                    tx.setAmount(new BigDecimal(Optional.ofNullable(object.getValue()).orElse(BigInteger.ZERO)).movePointLeft(18));
                    tx.setNativeToken(true);
                    tx.setGasUsed(Optional.ofNullable(object.getGas()).orElse(BigInteger.ZERO).longValue());
                    tx.setTxStatus(TxStatus.SUCCESS);
                    tx.setTxTime(LocalDateTime.now());
                    saved += saveIfAbsent(tx);
                }
            } catch (IOException exception) {
                throw new IllegalStateException("rpc sync failed at block " + blockNumber, exception);
            }
        }
        return new SyncResult(chainId, fromBlock, toBlock, saved, "rpc");
    }

    private int saveIfAbsent(ChainTransaction tx) {
        if (transactionRepository.existsByTxHashIgnoreCaseAndLogIndex(tx.getTxHash(), tx.getLogIndex())) {
            return 0;
        }
        RiskAssessment assessment = riskEngineService.assess(tx);
        tx.setRiskLevel(assessment.riskLevel());
        tx.setRiskReason(assessment.riskReason());
        transactionRepository.save(tx);
        return 1;
    }

    private ChainTransaction mockTransaction(int index, long blockNumber) {
        boolean nativeToken = index % 3 == 0;
        ChainTransaction tx = new ChainTransaction();
        tx.setChainId(11155111L);
        tx.setTxHash("0xsync" + String.format("%060d", blockNumber));
        tx.setBlockNumber(blockNumber);
        tx.setLogIndex((long) (index % 2));
        tx.setFromAddress(index % 2 == 0 ? "0x1111111111111111111111111111111111111111" : "0x2222222222222222222222222222222222222222");
        tx.setToAddress(index == 4 ? "0x9999999999999999999999999999999999999999" : "0x3333333333333333333333333333333333333333");
        tx.setTokenAddress(nativeToken ? null : "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        tx.setTokenSymbol(nativeToken ? "ETH" : "USDT");
        tx.setAmount(index == 7 ? new BigDecimal("120000") : new BigDecimal(index * 2500L + 100));
        tx.setNativeToken(nativeToken);
        tx.setGasUsed(index == 8 ? 260_000L : 21_000L + index * 5000L);
        tx.setTxStatus(index == 9 ? TxStatus.FAILED : TxStatus.SUCCESS);
        tx.setTxTime(LocalDateTime.now().minusMinutes(index));
        return tx;
    }

    private ChainLensProperties.ChainItem findChain(Long chainId) {
        return properties.getChains().stream()
                .filter(ChainLensProperties.ChainItem::isEnabled)
                .filter(item -> item.getChainId().equals(chainId))
                .min(Comparator.comparing(ChainLensProperties.ChainItem::getChainId))
                .orElseThrow(() -> new IllegalArgumentException("chain config not found: " + chainId));
    }
}

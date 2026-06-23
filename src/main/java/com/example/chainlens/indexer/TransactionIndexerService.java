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
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionIndexerService {
    private static final Long NATIVE_TRANSFER_LOG_INDEX = -1L;

    private final ChainLensProperties properties;
    private final ChainTransactionRepository transactionRepository;
    private final RiskEngineService riskEngineService;
    private final Erc20TransferParser erc20TransferParser;

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
                LocalDateTime blockTime = toBlockTime(block.getBlock().getTimestamp());
                for (EthBlock.TransactionResult<?> result : block.getBlock().getTransactions()) {
                    EthBlock.TransactionObject object = (EthBlock.TransactionObject) result.get();
                    Optional<TransactionReceipt> receipt = fetchReceipt(web3j, object.getHash());
                    TxStatus txStatus = receipt.map(this::toTxStatus).orElse(TxStatus.PENDING);
                    Long gasUsed = receipt.map(TransactionReceipt::getGasUsed)
                            .map(BigInteger::longValue)
                            .orElse(null);

                    BigInteger nativeValue = Optional.ofNullable(object.getValue()).orElse(BigInteger.ZERO);
                    if (nativeValue.signum() > 0) {
                        ChainTransaction nativeTransfer = new ChainTransaction();
                        nativeTransfer.setChainId(chainId);
                        nativeTransfer.setTxHash(object.getHash());
                        nativeTransfer.setBlockNumber(blockNumber);
                        nativeTransfer.setLogIndex(NATIVE_TRANSFER_LOG_INDEX);
                        nativeTransfer.setFromAddress(object.getFrom());
                        nativeTransfer.setToAddress(object.getTo());
                        nativeTransfer.setTokenSymbol(chain.getNativeSymbol());
                        nativeTransfer.setAmount(new BigDecimal(nativeValue).movePointLeft(18));
                        nativeTransfer.setNativeToken(true);
                        nativeTransfer.setGasUsed(gasUsed);
                        nativeTransfer.setTxStatus(txStatus);
                        nativeTransfer.setTxTime(blockTime);
                        saved += saveIfAbsent(nativeTransfer);
                    }

                    if (receipt.isPresent()) {
                        for (Log log : receipt.get().getLogs()) {
                            saved += saveErc20TransferIfPresent(chainId, object.getHash(), blockNumber, blockTime, gasUsed, txStatus, log);
                        }
                    }
                }
            } catch (IOException exception) {
                throw new IllegalStateException("rpc sync failed at block " + blockNumber, exception);
            }
        }
        return new SyncResult(chainId, fromBlock, toBlock, saved, "rpc");
    }

    private Optional<TransactionReceipt> fetchReceipt(Web3j web3j, String txHash) throws IOException {
        EthGetTransactionReceipt response = web3j.ethGetTransactionReceipt(txHash).send();
        return response.getTransactionReceipt();
    }

    private int saveErc20TransferIfPresent(Long chainId, String txHash, long blockNumber, LocalDateTime blockTime,
                                           Long gasUsed, TxStatus txStatus, Log log) {
        Optional<Erc20TransferEvent> event = erc20TransferParser.parse(log.getTopics(), log.getData());
        if (event.isEmpty()) {
            return 0;
        }

        ChainTransaction erc20Transfer = new ChainTransaction();
        erc20Transfer.setChainId(chainId);
        erc20Transfer.setTxHash(Optional.ofNullable(log.getTransactionHash()).orElse(txHash));
        erc20Transfer.setBlockNumber(Optional.ofNullable(log.getBlockNumber()).map(BigInteger::longValue).orElse(blockNumber));
        erc20Transfer.setLogIndex(Optional.ofNullable(log.getLogIndex()).map(BigInteger::longValue).orElse(0L));
        erc20Transfer.setFromAddress(event.get().fromAddress());
        erc20Transfer.setToAddress(event.get().toAddress());
        erc20Transfer.setTokenAddress(log.getAddress());
        erc20Transfer.setTokenSymbol("ERC20");
        erc20Transfer.setAmount(new BigDecimal(event.get().amount()));
        erc20Transfer.setNativeToken(false);
        erc20Transfer.setGasUsed(gasUsed);
        erc20Transfer.setTxStatus(txStatus);
        erc20Transfer.setTxTime(blockTime);
        return saveIfAbsent(erc20Transfer);
    }

    private TxStatus toTxStatus(TransactionReceipt receipt) {
        if (!receipt.isStatusOK()) {
            return TxStatus.FAILED;
        }
        return TxStatus.SUCCESS;
    }

    private LocalDateTime toBlockTime(BigInteger timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.longValue()), ZoneOffset.UTC);
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

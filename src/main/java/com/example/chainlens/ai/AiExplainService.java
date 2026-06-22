package com.example.chainlens.ai;

import com.example.chainlens.chain.ChainLensProperties;
import com.example.chainlens.common.NotFoundException;
import com.example.chainlens.transaction.ChainTransaction;
import com.example.chainlens.transaction.ChainTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiExplainService {
    private final ChainTransactionRepository transactionRepository;
    private final ChainLensProperties properties;

    public String explain(String txHash) {
        ChainTransaction tx = transactionRepository.findByTxHashIgnoreCase(txHash)
                .orElseThrow(() -> new NotFoundException("transaction not found"));
        String chainName = properties.getChains().stream()
                .filter(chain -> chain.getChainId().equals(tx.getChainId()))
                .map(ChainLensProperties.ChainItem::getChainName)
                .findFirst()
                .orElse("chain " + tx.getChainId());
        String txType = tx.isNativeToken() ? "native token transfer" : "ERC20 transfer";
        return "This is a " + txType + " on " + chainName + ". Sender address is "
                + tx.getFromAddress() + ", receiver address is " + tx.getToAddress()
                + ", transfer amount is " + tx.getAmount() + " " + tx.getTokenSymbol()
                + ". Transaction status is " + tx.getTxStatus() + ", gas used is " + tx.getGasUsed()
                + ". The transaction risk level is " + tx.getRiskLevel()
                + ", risk reason: " + tx.getRiskReason()
                + ". This endpoint currently uses a template and is ready for future OpenAI/Qwen/DeepSeek integration.";
    }
}

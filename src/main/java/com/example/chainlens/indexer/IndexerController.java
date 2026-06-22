package com.example.chainlens.indexer;

import com.example.chainlens.indexer.dto.SyncRangeRequest;
import com.example.chainlens.indexer.dto.SyncResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/indexer")
@RequiredArgsConstructor
public class IndexerController {
    private final TransactionIndexerService transactionIndexerService;

    @PostMapping("/mock-sync")
    public SyncResult mockSync() {
        return transactionIndexerService.mockSync();
    }

    @PostMapping("/sync-range")
    public SyncResult syncRange(@Valid @RequestBody SyncRangeRequest request) {
        return transactionIndexerService.syncRange(request.chainId(), request.fromBlock(), request.toBlock());
    }

    @GetMapping("/latest-block/{chainId}")
    public BigInteger latestBlock(@PathVariable Long chainId) {
        return transactionIndexerService.latestBlock(chainId);
    }
}

package com.example.chainlens.indexer.dto;

public record SyncResult(Long chainId, long fromBlock, long toBlock, int savedCount, String mode) {
}

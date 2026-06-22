package com.example.chainlens.indexer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SyncRangeRequest(
        @NotNull Long chainId,
        @NotNull @PositiveOrZero Long fromBlock,
        @NotNull @PositiveOrZero Long toBlock
) {
}

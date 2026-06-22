package com.example.chainlens.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WalletCreateRequest(
        @NotNull Long chainId,
        @NotBlank String address,
        String label,
        String remark
) {
}

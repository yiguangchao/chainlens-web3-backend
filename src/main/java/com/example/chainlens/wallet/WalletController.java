package com.example.chainlens.wallet;

import com.example.chainlens.wallet.dto.WalletCreateRequest;
import com.example.chainlens.wallet.dto.WalletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public WalletResponse create(@Valid @RequestBody WalletCreateRequest request) {
        return walletService.create(request);
    }

    @GetMapping
    public List<WalletResponse> list() {
        return walletService.list();
    }

    @GetMapping("/{address}")
    public WalletResponse getByAddress(@PathVariable String address) {
        return walletService.getByAddress(address);
    }
}

package com.example.chainlens.wallet;

import com.example.chainlens.common.NotFoundException;
import com.example.chainlens.wallet.dto.WalletCreateRequest;
import com.example.chainlens.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    @Transactional
    public WalletResponse create(WalletCreateRequest request) {
        if (walletRepository.existsByChainIdAndAddressIgnoreCase(request.chainId(), request.address())) {
            throw new IllegalArgumentException("wallet address already exists on this chain");
        }
        WalletAddress wallet = new WalletAddress();
        wallet.setChainId(request.chainId());
        wallet.setAddress(request.address());
        wallet.setLabel(request.label());
        wallet.setRemark(request.remark());
        return WalletResponse.from(walletRepository.save(wallet));
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> list() {
        return walletRepository.findAll().stream().map(WalletResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public WalletResponse getByAddress(String address) {
        return walletRepository.findByAddressIgnoreCase(address)
                .map(WalletResponse::from)
                .orElseThrow(() -> new NotFoundException("wallet address not found"));
    }
}

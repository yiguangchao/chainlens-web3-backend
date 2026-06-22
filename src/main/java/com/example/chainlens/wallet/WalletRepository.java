package com.example.chainlens.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletAddress, Long> {
    Optional<WalletAddress> findByAddressIgnoreCase(String address);
    boolean existsByChainIdAndAddressIgnoreCase(Long chainId, String address);
}

package com.example.chainlens.risk;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskBlacklistRepository extends JpaRepository<RiskBlacklist, Long> {
    Optional<RiskBlacklist> findByAddressIgnoreCase(String address);
}

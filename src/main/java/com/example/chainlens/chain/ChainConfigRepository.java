package com.example.chainlens.chain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChainConfigRepository extends JpaRepository<ChainConfig, Long> {
    List<ChainConfig> findByEnabledTrue();
}

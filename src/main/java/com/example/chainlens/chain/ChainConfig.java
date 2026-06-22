package com.example.chainlens.chain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chain_config")
public class ChainConfig {
    @Id
    @Column(name = "chain_id")
    private Long chainId;

    @Column(name = "chain_name", nullable = false)
    private String chainName;

    @Column(name = "rpc_url", nullable = false)
    private String rpcUrl;

    @Column(name = "native_symbol", nullable = false)
    private String nativeSymbol;

    @Column(name = "explorer_url")
    private String explorerUrl;

    private boolean enabled;
}

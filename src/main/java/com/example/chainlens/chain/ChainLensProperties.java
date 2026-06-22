package com.example.chainlens.chain;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "chainlens")
public class ChainLensProperties {
    private Indexer indexer = new Indexer();
    private List<ChainItem> chains = new ArrayList<>();

    public Indexer getIndexer() {
        return indexer;
    }

    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    public List<ChainItem> getChains() {
        return chains;
    }

    public void setChains(List<ChainItem> chains) {
        this.chains = chains;
    }

    public static class Indexer {
        private String mode = "mock";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    public static class ChainItem {
        private Long chainId;
        private String chainName;
        private String rpcUrl;
        private String nativeSymbol;
        private String explorerUrl;
        private boolean enabled;

        public Long getChainId() {
            return chainId;
        }

        public void setChainId(Long chainId) {
            this.chainId = chainId;
        }

        public String getChainName() {
            return chainName;
        }

        public void setChainName(String chainName) {
            this.chainName = chainName;
        }

        public String getRpcUrl() {
            return rpcUrl;
        }

        public void setRpcUrl(String rpcUrl) {
            this.rpcUrl = rpcUrl;
        }

        public String getNativeSymbol() {
            return nativeSymbol;
        }

        public void setNativeSymbol(String nativeSymbol) {
            this.nativeSymbol = nativeSymbol;
        }

        public String getExplorerUrl() {
            return explorerUrl;
        }

        public void setExplorerUrl(String explorerUrl) {
            this.explorerUrl = explorerUrl;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

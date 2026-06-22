package com.example.chainlens.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChainConfigService {
    private final ChainConfigRepository chainConfigRepository;
    private final ChainLensProperties properties;

    public List<ChainLensProperties.ChainItem> enabledChainsFromConfig() {
        return properties.getChains().stream()
                .filter(ChainLensProperties.ChainItem::isEnabled)
                .sorted(Comparator.comparing(ChainLensProperties.ChainItem::getChainId))
                .toList();
    }

    public List<ChainConfig> enabledChainsFromDb() {
        return chainConfigRepository.findByEnabledTrue();
    }
}

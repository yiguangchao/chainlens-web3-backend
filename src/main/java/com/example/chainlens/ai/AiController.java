package com.example.chainlens.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiExplainService aiExplainService;

    @GetMapping("/explain/{txHash}")
    public Map<String, String> explain(@PathVariable String txHash) {
        return Map.of("explanation", aiExplainService.explain(txHash));
    }
}

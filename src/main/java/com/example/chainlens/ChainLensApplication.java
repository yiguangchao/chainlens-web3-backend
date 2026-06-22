package com.example.chainlens;

import com.example.chainlens.chain.ChainLensProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ChainLensProperties.class)
public class ChainLensApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChainLensApplication.class, args);
    }
}

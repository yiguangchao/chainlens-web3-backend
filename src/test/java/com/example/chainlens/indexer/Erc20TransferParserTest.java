package com.example.chainlens.indexer;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Erc20TransferParserTest {

    private final Erc20TransferParser parser = new Erc20TransferParser();

    @Test
    void parseTransferLog() {
        List<String> topics = List.of(
                Erc20TransferParser.TRANSFER_TOPIC0,
                "0x0000000000000000000000001111111111111111111111111111111111111111",
                "0x0000000000000000000000002222222222222222222222222222222222222222"
        );
        String data = "0x0000000000000000000000000000000000000000000000000000000000000064";

        Erc20TransferEvent event = parser.parse(topics, data).orElseThrow();

        assertThat(event.fromAddress()).isEqualTo("0x1111111111111111111111111111111111111111");
        assertThat(event.toAddress()).isEqualTo("0x2222222222222222222222222222222222222222");
        assertThat(event.amount()).isEqualTo(BigInteger.valueOf(100));
    }
}

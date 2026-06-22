package com.example.chainlens.indexer;

import java.math.BigInteger;

public record Erc20TransferEvent(String fromAddress, String toAddress, BigInteger amount) {
}

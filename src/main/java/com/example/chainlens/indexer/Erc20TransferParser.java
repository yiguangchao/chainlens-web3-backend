package com.example.chainlens.indexer;

import org.springframework.stereotype.Component;
import org.web3j.crypto.Hash;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Component
public class Erc20TransferParser {
    public static final String TRANSFER_EVENT_SIGNATURE = "Transfer(address,address,uint256)";
    public static final String TRANSFER_TOPIC0 = Hash.sha3String(TRANSFER_EVENT_SIGNATURE);

    public boolean isTransferEvent(List<String> topics) {
        return topics != null && topics.size() >= 3 && TRANSFER_TOPIC0.equalsIgnoreCase(topics.get(0));
    }

    public Optional<Erc20TransferEvent> parse(List<String> topics, String data) {
        if (!isTransferEvent(topics) || data == null) {
            return Optional.empty();
        }

        /*
         * topic0: keccak256("Transfer(address,address,uint256)") event signature.
         * topic1: indexed from address, left padded to 32 bytes.
         * topic2: indexed to address, left padded to 32 bytes.
         * data: non-indexed uint256 value, the ERC20 transfer amount.
         */
        String from = topicToAddress(topics.get(1));
        String to = topicToAddress(topics.get(2));
        BigInteger amount = new BigInteger(cleanHex(data), 16);
        return Optional.of(new Erc20TransferEvent(from, to, amount));
    }

    private String topicToAddress(String topic) {
        String hex = cleanHex(topic);
        if (hex.length() < 40) {
            throw new IllegalArgumentException("invalid address topic");
        }
        return "0x" + hex.substring(hex.length() - 40).toLowerCase();
    }

    private String cleanHex(String value) {
        return value.startsWith("0x") || value.startsWith("0X") ? value.substring(2) : value;
    }
}

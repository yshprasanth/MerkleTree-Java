package com.blockchain.merkletree.common;

import org.springframework.stereotype.Component;

@Component
public class MerkleException extends RuntimeException {

    public MerkleException(String message) {
        super(message);
    }
}

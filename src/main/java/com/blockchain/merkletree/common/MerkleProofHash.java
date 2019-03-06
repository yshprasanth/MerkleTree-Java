package com.blockchain.merkletree.common;

public class MerkleProofHash {

    public enum Branch{
        LEFT, RIGHT, OLDROOT;
    }

    private MerkleHash hash;
    private Branch direction;

    public MerkleHash getHash() {
        return hash;
    }

    public Branch getDirection() {
        return direction;
    }

    public MerkleProofHash(MerkleHash hash, Branch direction) {
        this.hash = hash;
        this.direction = direction;
    }

    @Override
    public String toString() {
        return hash.toString();
    }
}

package com.blockchain.merkletree.common;

import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;

import java.util.Arrays;

public class MerkleHash {

    private byte[] value;

    private MerkleHash() {

    }

    public static MerkleHash create(byte[] buffer){
        MerkleHash mh = new MerkleHash();
        mh.computeHash(buffer);
        return mh;
    }

    public static MerkleHash create(String data) {
        return create(data.getBytes());
    }

    public static MerkleHash create(MerkleHash left, MerkleHash right) {
        return create(Bytes.concat(left.value, right.value));
    }

    public byte[] getValue() {
        return value;
    }

    protected void setValue(byte[] value) {
        Handler.check(()->value.length==Constants.HASH_LENGTH
                , "Invalid Hash Length");
        this.value = value;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Handler.check(() -> obj instanceof MerkleHash
                ,"Not a valid MerkleHash object") ;

        return super.equals(obj);
    }

    public boolean equals(byte[] hash){
        return Arrays.equals(value, hash);
    }

    public boolean equals(MerkleHash merkleHash){
        if(merkleHash!=null){
            return Arrays.equals(value, merkleHash.value);
        } else
            return false;
    }

    @Override
    public String toString() {
        return Arrays.toString(value).replace("-","");
    }

    public void computeHash(byte[] buffer) {
        byte[] hash = Hashing.sha256().hashBytes(buffer).asBytes();
        setValue(hash);
    }
}

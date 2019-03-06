package com.blockchain.merkletree.common;



public class Handler {

    public static void check(Condition<Boolean> condition, String message){
        if(!condition.execute())
            throw new MerkleException(message);
    }
}

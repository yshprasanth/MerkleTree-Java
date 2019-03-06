package com.blockchain.merkletree.common;

@FunctionalInterface
public interface Condition<T> {
    T execute();
}

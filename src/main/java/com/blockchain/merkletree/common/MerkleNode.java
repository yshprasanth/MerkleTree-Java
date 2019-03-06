package com.blockchain.merkletree.common;

import com.google.common.primitives.Bytes;

import java.util.Iterator;
import java.util.Stack;

public class MerkleNode implements Iterable<MerkleNode> {

    private MerkleHash hash;
    private MerkleNode leftNode;
    private MerkleNode rightNode;
    private MerkleNode parentNode;

    public MerkleNode() {
    }

    public MerkleNode(MerkleHash hash) {
        this.hash = hash;
    }

    public MerkleNode(MerkleNode leftNode, MerkleNode rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.leftNode.parentNode = this;
        if (this.rightNode != null)
            this.rightNode.parentNode = this;
        computeHash();
    }

    public boolean equals(MerkleNode node) {
        return hash.equals(node.hash);
    }

    public boolean verifyHash() {
        if (leftNode == null && rightNode == null) {
            return true;
        }

        if (rightNode == null) {
            return hash.equals(leftNode.hash);
        }

        Handler.check(() -> leftNode != null, "Left branch must be a node, if right branch is a node");

        MerkleHash leftRightHash = MerkleHash.create(leftNode.hash, rightNode.hash);
        return hash.equals(leftRightHash);
    }

    public boolean canVerifyHash() {
        return (leftNode != null && rightNode != null) || (leftNode != null);
    }

    public boolean isLeaf() {
        return leftNode == null && rightNode == null;
    }

    public void setRightNode(MerkleNode node) {
        Handler.check(() -> node.hash != null, "Node hash must be initialized.");
        rightNode = node;
        rightNode.parentNode = this;

        if (leftNode != null)
            computeHash();
    }

    public void setLeftNode(MerkleNode node) {
        Handler.check(() -> node.hash != null, "node hash must be initialized.");
        leftNode = node;
        leftNode.parentNode = this;
        computeHash();
    }

    public Iterable<MerkleNode> leaves() {
        return this;
    }

    @Override
    public String toString() {
        return hash.toString();
    }

    protected void computeHash() {
        hash = rightNode == null
                ? leftNode.hash
                : MerkleHash.create(Bytes.concat(leftNode.hash.getValue(), rightNode.hash.getValue()));

        if (parentNode != null) {
            parentNode.computeHash();
        }
    }

    public MerkleHash computeHash(byte[] buffer) {
        hash = MerkleHash.create(buffer);
        return hash;
    }

    @Override
    public Iterator<MerkleNode> iterator() {
        return new MerkleNodeIterator(this);
    }

    public MerkleHash getHash() {
        return hash;
    }

    public MerkleNode getLeftNode() {
        return leftNode;
    }

    public MerkleNode getRightNode() {
        return rightNode;
    }

    public MerkleNode getParentNode() {
        return parentNode;
    }

}

class MerkleNodeIterator implements Iterator<MerkleNode> {
    Stack<MerkleNode> st;


    public MerkleNodeIterator(MerkleNode root) {
        Stack st = new Stack<MerkleNode>();
        st.push(root);
        st.push(root.getLeftNode());
        st.push(root.getRightNode());
    }

    @Override
    public boolean hasNext() {
        return !st.empty();
    }

    @Override
    public MerkleNode next() {
        return st.pop();
    }

    @Override
    public void remove() {
        throw new java.lang.UnsupportedOperationException("Remove not supported.");
    }
}

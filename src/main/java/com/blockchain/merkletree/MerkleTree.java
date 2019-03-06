package com.blockchain.merkletree;

import com.blockchain.merkletree.common.MerkleHash;
import com.blockchain.merkletree.common.MerkleNode;
import com.blockchain.merkletree.common.MerkleProofHash;
import com.google.common.primitives.Bytes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.blockchain.merkletree.common.Handler.check;

public class MerkleTree {

    private MerkleNode rootNode;

    protected List<MerkleNode> nodes;
    protected List<MerkleNode> leaves;

    public MerkleTree() {
        nodes = new ArrayList<>();
        leaves = new ArrayList<>();
    }

    public MerkleNode appendLeaf(MerkleNode node) {
        nodes.add(node);
        leaves.add(node);

        return node;
    }

    public void appendLeaves(MerkleNode[] nodes) {
        Arrays.stream(nodes).forEach(n -> appendLeaf(n));
    }

    public MerkleNode appendLeaf(MerkleHash hash) {
        return appendLeaf(createNode(hash));
    }

    public void appendLeaves(MerkleHash[] hashes) {
        Arrays.stream(hashes).forEach(h -> appendLeaf(h));
    }

    public MerkleHash addTree(MerkleTree tree) {
        check(() -> leaves.size() > 0, "Cannot add to a tree with no leaves.");

        tree.leaves.forEach(l -> appendLeaf(l));
        return buildTree();
    }

    public void fixOddNumberLeaves() {
        if (leaves.size() % 2 == 1) {
            appendLeaf(leaves.get(leaves.size() - 1).getHash());
        }
    }

    public MerkleHash buildTree() {
        check(() -> leaves.size() > 0, "Cannot build tree with no leaves.");
        buildTree(leaves);

        return rootNode.getHash();
    }

    protected void buildTree(List<MerkleNode> nodes) {
        check(() -> nodes.size() > 0, "node list not expected to be empty.");
        if (nodes.size() == 1) {
            rootNode = nodes.get(0);
        } else {
            List<MerkleNode> parents = new ArrayList<>();

            for (int i = 0; i < nodes.size(); i += 2) {
                MerkleNode right = (i + 1 < nodes.size()) ? nodes.get(i + 1) : null;
                MerkleNode parent = createNode(nodes.get(i), right);
                parents.add(parent);
            }
            buildTree(parents);
        }
    }

    public List<MerkleProofHash> auditProof(MerkleHash merkleHash) {
        List<MerkleProofHash> auditTrail = new ArrayList<>();
        var leafNode = findLeaf(merkleHash);
        if (leafNode != null) {
            check(() -> leafNode.getParentNode() != null, "Expected leaf to have a parent.");
            buildAuditTrail(auditTrail, leafNode.getParentNode(), leafNode);
        }
        return auditTrail;
    }

    private void buildAuditTrail(List<MerkleProofHash> auditTrail, MerkleNode parentNode, MerkleNode leafNode) {
        if (parentNode != null) {
            check(() -> leafNode.getParentNode() == parentNode, "Parent of child is not expected parent.");

            var nextChild = parentNode.getLeftNode() == leafNode ? parentNode.getRightNode() : parentNode.getLeftNode();
            var direction = parentNode.getLeftNode() == leafNode ? MerkleProofHash.Branch.LEFT : MerkleProofHash.Branch.RIGHT;

            if (nextChild != null) {
                auditTrail.add(new MerkleProofHash(nextChild.getHash(), direction));
            }

            buildAuditTrail(auditTrail, leafNode.getParentNode().getParentNode(), leafNode.getParentNode());
        }
    }

    public List<MerkleProofHash> consistencyProof(int m) {
        List<MerkleProofHash> hashNodes = new ArrayList<>();
        int index = (int) Math.log(m);

        MerkleNode node = leaves.get(0);
        while (index > 0) {
            node = node.getParentNode();
            --index;
        }

        int k = 2; //node.leaves().count()

        hashNodes.add(new MerkleProofHash(node.getHash(), MerkleProofHash.Branch.OLDROOT));
        if (m == k) {

        } else {
            MerkleNode sn = node.getParentNode().getRightNode();
            boolean traverseTree = true;

            while (traverseTree) {
                //check(() -> sn != null, "Sibling node must exist because m!=k");
                int snCount = 2; //sn.leaves().count;
                if (m - k == snCount) {
                    hashNodes.add(new MerkleProofHash(sn.getHash(), MerkleProofHash.Branch.OLDROOT));
                    break;
                }

                if (m - k > snCount) {
                    hashNodes.add(new MerkleProofHash(sn.getHash(), MerkleProofHash.Branch.OLDROOT));
                    sn = sn.getParentNode().getRightNode();
                    k += snCount;
                } else {
                    sn = sn.getLeftNode();
                }
            }
        }
        return hashNodes;
    }

    public List<MerkleProofHash> consistencyAuditProof(MerkleHash nodeHash) {
        List<MerkleProofHash> auditTrail = new ArrayList<>();
        var node = rootNode.getLeftNode(); // Single(n => n.Hash == nodeHash);
        var parentNode = node.getParentNode();
        buildAuditTrail(auditTrail, parentNode, node);
        return auditTrail;
    }

    private MerkleNode findLeaf(MerkleHash merkleHash) {
        return leaves.stream().filter(l -> l.getHash() == merkleHash).findFirst().get();
    }

    public static boolean verifyAudit(MerkleHash rootHash, MerkleHash leafHash, List<MerkleProofHash> auditTrail) {
        check(() -> auditTrail.size() > 0, "Audit trail cannot be empty.");
        MerkleHash testHash = leafHash;
        for (MerkleProofHash auditHash : auditTrail) {
            testHash = auditHash.getDirection() == MerkleProofHash.Branch.LEFT ?
                    MerkleHash.create(Bytes.concat(testHash.getValue(), auditHash.getHash().getValue())) :
                    MerkleHash.create(Bytes.concat(auditHash.getHash().getValue(), testHash.getValue()));
        }

        return rootHash.equals(testHash);
    }

    public static boolean VerifyConsistency(MerkleHash oldRootHash, List<MerkleProofHash> proof) {
        MerkleHash hash, lhash, rhash;

        if (proof.size() > 1) {
            lhash = proof.get(proof.size() - 2).getHash();
            int hidx = proof.size() - 1;
            hash = rhash = MerkleTree.computeHash(lhash, proof.get(hidx).getHash());
            hidx -= 2;

            // foreach (var nextHashNode in proof.Skip(1))
            while (hidx >= 0) {
                lhash = proof.get(hidx).getHash();
                hash = rhash = MerkleTree.computeHash(lhash, rhash);

                --hidx;
            }
        } else {
            hash = proof.get(0).getHash();
        }

        return hash == oldRootHash;
    }

    public static MerkleHash computeHash(MerkleHash left, MerkleHash right) {
        return MerkleHash.create(Bytes.concat(left.getValue(), right.getValue()));
    }

    protected MerkleNode createNode(MerkleHash hash) {
        return new MerkleNode(hash);
    }

    protected MerkleNode createNode(MerkleNode left, MerkleNode right) {
        return new MerkleNode(left, right);
    }


}

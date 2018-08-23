//====================================================================================================
//The Free Edition of C# to Java Converter limits conversion output to 100 lines per file.

//To purchase the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================

package Clifton.Blockchain;

import java.util.*;

/* 
* Copyright (c) Marc Clifton
* The Code Project Open License (CPOL) 1.02
* http://www.codeproject.com/info/cpol10.aspx
*/


import Clifton.Core.ExtensionMethods.*;

public class MerkleTree
{
	private MerkleNode RootNode;
	public final MerkleNode getRootNode()
	{
		return RootNode;
	}
	protected final void setRootNode(MerkleNode value)
	{
		RootNode = value;
	}

	protected ArrayList<MerkleNode> nodes;
	protected ArrayList<MerkleNode> leaves;

	public static void Contract(tangible.Func0Param<Boolean> action, String msg)
	{
		if (!action.invoke())
		{
			throw new MerkleException(msg);
		}
	}

	public MerkleTree()
	{
		nodes = new ArrayList<MerkleNode>();
		leaves = new ArrayList<MerkleNode>();
	}

	public final MerkleNode AppendLeaf(MerkleNode node)
	{
		nodes.add(node);
		leaves.add(node);

		return node;
	}

	public final void AppendLeaves(MerkleNode[] nodes)
	{
		nodes.ForEach(n -> AppendLeaf(n));
	}

	public final MerkleNode AppendLeaf(MerkleHash hash)
	{
		Clifton.Blockchain.MerkleNode node = CreateNode(hash);
		nodes.add(node);
		leaves.add(node);

		return node;
	}

	public final ArrayList<MerkleNode> AppendLeaves(MerkleHash[] hashes)
	{
		ArrayList<MerkleNode> nodes = new ArrayList<MerkleNode>();
		hashes.ForEach(h -> nodes.add(AppendLeaf(h)));

		return nodes;
	}

	public final MerkleHash AddTree(MerkleTree tree)
	{
		Contract(() -> !leaves.isEmpty(), "Cannot add to a tree with no leaves.");
		tree.leaves.forEach(l -> AppendLeaf(l));

		return BuildTree();
	}

	/** 
	 If we have an odd number of leaves, add a leaf that
	 is a duplicate of the last leaf hash so that when we add the leaves of the new tree,
	 we don't change the root hash of the current tree.
	 This method should only be used if you have a specific reason that you need to balance
	 the last node with it's right branch, for example as a pre-step to computing an audit trail
	 on the last leaf of an odd number of leaves in the tree.
	*/
	public final void FixOddNumberLeaves()
	{
		if ((leaves.size() & 1) == 1)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var lastLeaf = leaves.get(leaves.size() - 1);
			Clifton.Blockchain.MerkleNode l = AppendLeaf(lastLeaf.Hash);
			// l.Text = lastLeaf.Text;
		}
	}

	/** 
	 Builds the tree for leaves and returns the root node.
	*/
	public final MerkleHash BuildTree()
	{
		// We do not call FixOddNumberLeaves because we want the ability to append 
		// leaves and add additional trees without creating unecessary wasted space in the tree.
		Contract(() -> !leaves.isEmpty(), "Cannot build a tree with no leaves.");
		BuildTree(leaves);

		return getRootNode().getHash();
	}

	// Why would we need this?
	//public void RegisterRoot(MerkleNode node)
	//{
	//    Contract(() => node.Parent == null, "Node is not a root node.");
	//    rootNode = node;
	//}

	/** 
	 Returns the audit proof hashes to reconstruct the root hash.
	 
	 @param leafHash The leaf hash we want to verify exists in the tree.
	 @return The audit trail of hashes needed to create the root, or an empty list if the leaf hash doesn't exist.
	*/
	public final ArrayList<MerkleProofHash> AuditProof(MerkleHash leafHash)
	{
		ArrayList<MerkleProofHash> auditTrail = new ArrayList<MerkleProofHash>();

		Clifton.Blockchain.MerkleNode leafNode = FindLeaf(leafHash);

		if (leafNode != null)
		{
			Contract(() -> leafNode.getParent() != null, "Expected leaf to have a parent.");
			Clifton.Blockchain.MerkleNode parent = leafNode.getParent();
			BuildAuditTrail(auditTrail, parent, leafNode);
		}

		return auditTrail;
	}

	/** 
	 Verifies ordering and consistency of the first n leaves, such that we reach the expected subroot.
	 This verifies that the prior data has not been changed and that leaf order has been preserved.
	 m is the number of leaves for which to do a consistency check.
	*/
	public final ArrayList<MerkleProofHash> ConsistencyProof(int m)
	{
		// Rule 1:
		// Find the leftmost node of the tree from which we can start our consistency proof.
		// Set k, the number of leaves for this node.
		ArrayList<MerkleProofHash> hashNodes = new ArrayList<MerkleProofHash>();
		int idx = (int)Math.log(m, 2);

		// Get the leftmost node.
		MerkleNode node = leaves.get(0);

		// Traverse up the tree until we get to the node specified by idx.
		while (idx > 0)
		{
			node = node.getParent();
			--idx;
		}

		int k = node.Leaves().size();
		hashNodes.add(new MerkleProofHash(node.getHash(), MerkleProofHash.Branch.OldRoot));

		if (m == k)
		{
			// Continue with Rule 3 -- the remainder is the audit proof
		}
		else
		{
			// Rule 2:
			// Set the initial sibling node (SN) to the sibling of the node acquired by Rule 1.
			// if m-k == # of SN's leaves, concatenate the hash of the sibling SN and exit Rule 2, as this represents the hash of the old root.
			// if m - k < # of SN's leaves, set SN to SN's left child node and repeat Rule 2.

			// sibling node:
			MerkleNode sn = node.getParent().getRightNode();
			boolean traverseTree = true;

			while (traverseTree)
			{
				Contract(() -> sn != null, "Sibling node must exist because m != k");
				int sncount = sn.Leaves().size();

				if (m - k == sncount)
				{
					hashNodes.add(new MerkleProofHash(sn.getHash(), MerkleProofHash.Branch.OldRoot));
					break;
				}

				if (m - k > sncount)
				{
					hashNodes.add(new MerkleProofHash(sn.getHash(), MerkleProofHash.Branch.OldRoot));
					sn = sn.getParent().getRightNode();
					k += sncount;
				}
				else // (m - k < sncount)
				{
					sn = sn.getLeftNode();
				}
			}
		}

		// Rule 3: Apply ConsistencyAuditProof below.

		return hashNodes;
	}

	/** 
	 Completes the consistency proof with an audit proof using the last node in the consistency proof.
	*/
	public final ArrayList<MerkleProofHash> ConsistencyAuditProof(MerkleHash nodeHash)
	{
		ArrayList<MerkleProofHash> auditTrail = new ArrayList<MerkleProofHash>();

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var node = getRootNode().Single(n = Clifton.Blockchain.MerkleHash.OpEquality(> n.Hash, nodeHash));
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var parent = node.Parent;
		BuildAuditTrail(auditTrail, parent, node);

		return auditTrail;
	}

	/** 
	 Verify that if we walk up the tree from a particular leaf, we encounter the expected root hash.
	*/
	public static boolean VerifyAudit(MerkleHash rootHash, MerkleHash leafHash, ArrayList<MerkleProofHash> auditTrail)
	{
		Contract(() -> !auditTrail.isEmpty(), "Audit trail cannot be empty.");
		MerkleHash testHash = leafHash;

		// TODO: Inefficient - compute hashes directly.
		for (MerkleProofHash auditHash : auditTrail)
		{
			testHash = auditHash.getDirection() == MerkleProofHash.Branch.Left ? MerkleHash.Create(testHash.getValue().Concat(auditHash.getHash().getValue()).ToArray()) : MerkleHash.Create(auditHash.getHash().getValue().Concat(testHash.getValue()).ToArray());
		}

		return Clifton.Blockchain.MerkleHash.OpEquality(rootHash, testHash);
	}

	/** 
	 For demo / debugging purposes, we return the pairs of hashes used to verify the audit proof.
	*/
	public static ArrayList<Tuple<MerkleHash, MerkleHash>> AuditHashPairs(MerkleHash leafHash, ArrayList<MerkleProofHash> auditTrail)
	{
		Contract(() -> !auditTrail.isEmpty(), "Audit trail cannot be empty.");
		ArrayList<Tuple<MerkleHash, MerkleHash>> auditPairs = new ArrayList<Tuple<MerkleHash, MerkleHash>>();
		MerkleHash testHash = leafHash;

		// TODO: Inefficient - compute hashes directly.
		for (MerkleProofHash auditHash : auditTrail)
		{
			switch (auditHash.getDirection())
			{
				case Left:
					auditPairs.add(new Tuple<MerkleHash, MerkleHash>(testHash, auditHash.getHash()));
					testHash = MerkleHash.Create(testHash.getValue().Concat(auditHash.getHash().getValue()).ToArray());
					break;


//====================================================================================================
//End of the allowed output for the Free Edition of C# to Java Converter.

//To purchase the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================

package Clifton.Blockchain;

import Clifton.Core.ExtensionMethods.*;
import java.util.*;

/* 
* Copyright (c) Marc Clifton
* The Code Project Open License (CPOL) 1.02
* http: //www.codeproject.com/info/cpol10.aspx
*/



//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
public class MerkleNode implements java.lang.Iterable<MerkleNode>
{
	private MerkleHash Hash;
	public final MerkleHash getHash()
	{
		return Hash;
	}
	protected final void setHash(MerkleHash value)
	{
		Hash = value;
	}
	private MerkleNode LeftNode;
	public final MerkleNode getLeftNode()
	{
		return LeftNode;
	}
	protected final void setLeftNode(MerkleNode value)
	{
		LeftNode = value;
	}
	private MerkleNode RightNode;
	public final MerkleNode getRightNode()
	{
		return RightNode;
	}
	protected final void setRightNode(MerkleNode value)
	{
		RightNode = value;
	}
	private MerkleNode Parent;
	public final MerkleNode getParent()
	{
		return Parent;
	}
	protected final void setParent(MerkleNode value)
	{
		Parent = value;
	}

	public final boolean getIsLeaf()
	{
		return getLeftNode() == null && getRightNode() == null;
	}

	public MerkleNode()
	{
	}

	/** 
	 Constructor for a base node (leaf), representing the lowest level of the tree.
	*/
	public MerkleNode(MerkleHash hash)
	{
		setHash(hash);
	}

	/**                 
	 Constructor for a parent node.
	*/

	public MerkleNode(MerkleNode left)
	{
		this(left, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public MerkleNode(MerkleNode left, MerkleNode right = null)
	public MerkleNode(MerkleNode left, MerkleNode right)
	{
		setLeftNode(left);
		setRightNode(right);
		getLeftNode().setParent(this);
		getRightNode().IfNotNull(r -> r.Parent = this);
		ComputeHash();
	}

	@Override
	public String toString()
	{
		return getHash().toString();
	}

	public final Iterator GetEnumerator()
	{
		return GetEnumerator();
	}

	public final Iterator<MerkleNode> iterator()
	{
		for (MerkleNode n : Iterate(this))
		{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
			yield return n;
		}
	}

	/** 
	 Bottom-up/left-right iteration of the tree.
	 
	 @param node
	 @return 
	*/
	protected final java.lang.Iterable<MerkleNode> Iterate(MerkleNode node)
	{
		if (node.getLeftNode() != null)
		{
			for (MerkleNode n : Iterate(node.getLeftNode()))
			{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
				yield return n;
			}
		}

		if (node.getRightNode() != null)
		{
			for (MerkleNode n : Iterate(node.getRightNode()))
			{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
				yield return n;
			}
		}

//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
		yield return node;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public MerkleHash ComputeHash(byte[] buffer)
	public final MerkleHash ComputeHash(byte[] buffer)
	{
		setHash(MerkleHash.Create(buffer));

		return getHash();
	}

	/** 
	 Return the leaves (not all children, just leaves) under this node
	*/
	public final java.lang.Iterable<MerkleNode> Leaves()
	{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
		return this.Where(n -> n.LeftNode == null && n.RightNode == null);
	}

	public final void SetLeftNode(MerkleNode node)
	{
		MerkleTree.Contract(() -> node.getHash() != null, "Node hash must be initialized.");
		setLeftNode(node);
		getLeftNode().setParent(this);
		ComputeHash();
	}

	public final void SetRightNode(MerkleNode node)
	{
		MerkleTree.Contract(() -> node.getHash() != null, "Node hash must be initialized.");
		setRightNode(node);
		getRightNode().setParent(this);

		// Can't compute hash if the left node isn't set yet.
		if (getLeftNode() != null)
		{
			ComputeHash();
		}
	}

	/** 
	 True if we have enough data to verify our hash, particularly if we have child nodes.
	 
	 @return True if this node is a leaf or a branch with at least a left node.
	*/
	public final boolean CanVerifyHash()
	{
		return (getLeftNode() != null && getRightNode() != null) || (getLeftNode() != null);
	}

	/** 
	 Verifies the hash for this node against the computed hash for our child nodes.
	 If we don't have any children, the return is always true because we have nothing to verify against.
	*/
	public final boolean VerifyHash()
	{
		if (getLeftNode() == null && getRightNode() == null)
		{
			return true;
		}

		if (getRightNode() == null)
		{
			return getHash().equals(getLeftNode().getHash());
		}

		MerkleTree.Contract(() -> getLeftNode() != null, "Left branch must be a node if right branch is a node.");
		MerkleHash leftRightHash = MerkleHash.Create(getLeftNode().getHash(), getRightNode().getHash());

		return getHash().equals(leftRightHash);
	}

	/** 
	 If the hashes are equal, we know the entire node tree is equal.
	*/
	public final boolean equals(MerkleNode node)
	{
		return getHash().equals(node.getHash());
	}

	protected final void ComputeHash()
	{
		// Repeat the left node if the right node doesn't exist.
		// This process breaks the case of doing a consistency check on 3 leaves when there are only 3 leaves in the tree.
		//MerkleHash rightHash = RightNode == null ? LeftNode.Hash : RightNode.Hash;
		//Hash = MerkleHash.Create(LeftNode.Hash.Value.Concat(rightHash.Value).ToArray());

		// Alternativately, do not repeat the left node, but carry the left node's hash up.
		// This process does not break the edge case described above.
		// We're implementing this version because the consistency check unit tests pass when we don't simulate
		// a right-hand node.
		setHash(getRightNode() == null ? getLeftNode().getHash() : MerkleHash.Create(getLeftNode().getHash().getValue().Concat(getRightNode().getHash().getValue()).ToArray()));
		getParent() == null ? null : getParent().ComputeHash(); // Recurse, because out hash has changed.
	}
}
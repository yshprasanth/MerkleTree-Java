package Clifton.Blockchain;

/* 
* Copyright (c) Marc Clifton
* The Code Project Open License (CPOL) 1.02
* http: //www.codeproject.com/info/cpol10.aspx
*/

public class MerkleProofHash
{
	public enum Branch
	{
		Left,
		Right,
		OldRoot; // used for linear list of hashes to compute the old root in a consistency proof.

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static Branch forValue(int value)
		{
			return values()[value];
		}
	}

	private MerkleHash Hash;
	public final MerkleHash getHash()
	{
		return Hash;
	}
	protected final void setHash(MerkleHash value)
	{
		Hash = value;
	}
	private Branch Direction = Branch.values()[0];
	public final Branch getDirection()
	{
		return Direction;
	}
	protected final void setDirection(Branch value)
	{
		Direction = value;
	}

	public MerkleProofHash(MerkleHash hash, Branch direction)
	{
		setHash(hash);
		setDirection(direction);
	}

	@Override
	public String toString()
	{
		return getHash().toString();
	}
}
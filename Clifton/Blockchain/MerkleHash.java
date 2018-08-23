package Clifton.Blockchain;

/* 
* Copyright (c) Marc Clifton
* The Code Project Open License (CPOL) 1.02
* http: //www.codeproject.com/info/cpol10.aspx
*/


public class MerkleHash
{
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private byte[] Value;
	private byte[] Value;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte[] getValue()
	public final byte[] getValue()
	{
		return Value;
	}
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: protected void setValue(byte[] value)
	protected final void setValue(byte[] value)
	{
		Value = value;
	}

	protected MerkleHash()
	{
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public static MerkleHash Create(byte[] buffer)
	public static MerkleHash Create(byte[] buffer)
	{
		MerkleHash hash = new MerkleHash();
		hash.ComputeHash(buffer);

		return hash;
	}

	public static MerkleHash Create(String buffer)
	{
		return Create(buffer.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	public static MerkleHash Create(MerkleHash left, MerkleHash right)
	{
		return Create(left.getValue().Concat(right.getValue()).ToArray());
	}

	public static boolean OpEquality(MerkleHash h1, MerkleHash h2)
	{
		return h1.equals(h2);
	}

	public static boolean OpInequality(MerkleHash h1, MerkleHash h2)
	{
		return !h1.equals(h2);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		MerkleTree.Contract(() -> obj instanceof MerkleHash, "rvalue is not a MerkleHash");
		return Equals((MerkleHash)obj);
	}

	@Override
	public String toString()
	{
		return BitConverter.toString(getValue()).replace("-", "");
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void ComputeHash(byte[] buffer)
	public final void ComputeHash(byte[] buffer)
	{
		SHA256 sha256 = SHA256.Create();
		SetHash(sha256.ComputeHash(buffer));
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void SetHash(byte[] hash)
	public final void SetHash(byte[] hash)
	{
		MerkleTree.Contract(() -> hash.length == Constants.HASH_LENGTH, "Unexpected hash length.");
		setValue(hash);
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public bool Equals(byte[] hash)
	public final boolean equals(byte[] hash)
	{
//C# TO JAVA CONVERTER WARNING: Java Arrays.equals is not always identical to LINQ 'SequenceEqual':
//ORIGINAL LINE: return Value.SequenceEqual(hash);
		return Arrays.equals(getValue(), hash);
	}

	public final boolean equals(MerkleHash hash)
	{
		boolean ret = false;

		if (((Object)hash) != null)
		{
//C# TO JAVA CONVERTER WARNING: Java Arrays.equals is not always identical to LINQ 'SequenceEqual':
//ORIGINAL LINE: ret = Value.SequenceEqual(hash.Value);
			ret = Arrays.equals(getValue(), hash.getValue());
		}

		return ret;
	}
}
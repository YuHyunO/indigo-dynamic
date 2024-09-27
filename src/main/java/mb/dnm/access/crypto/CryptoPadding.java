package mb.dnm.access.crypto;

public interface CryptoPadding {

	public byte[] addPadding(byte[] source, int blockSize);

	public byte[] removePadding(byte[] source, int blockSize);

}

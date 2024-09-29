package mb.dnm.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AdaptorTemplate#com.indigo.temp.core.util
 * */
public class GZipUtils {

	public static byte[] gzip(byte[] in) throws Exception {
		if (in != null && in.length > 0) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			CompressorOutputStream gout = new CompressorStreamFactory()
					.createCompressorOutputStream(CompressorStreamFactory.BZIP2, bout);
			gout.write(in);
			gout.flush();
			gout.close();
			byte[] ret = bout.toByteArray();
			return ret;
		}
		return new byte[0];
	}

	public static byte[] gzip(Object in) throws Exception {
		if (in != null) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			CompressorOutputStream gout = new CompressorStreamFactory()
					.createCompressorOutputStream(CompressorStreamFactory.BZIP2, bout);
			ObjectOutputStream oos = new ObjectOutputStream(gout);
			oos.writeObject(in);
			gout.flush();
			gout.close();
			byte[] ret = bout.toByteArray();
			return ret;
		}
		return new byte[0];
	}

	public static Object gzipToObj(byte[] in) throws Exception {
		if (in != null) {
			byte[] org = gunzip(in);
			ByteArrayInputStream bis = new ByteArrayInputStream(org);
			ObjectInput oin = new ObjectInputStream(bis);
			Object obj = oin.readObject();
			return obj;
		}
		return null;
	}

	public static byte[] gunzip(byte[] in) throws Exception {
		if (in != null && in.length > 0) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] tmp = new byte[4 * 1024];

			ByteArrayInputStream bin = new ByteArrayInputStream(in);
			CompressorInputStream gin = new CompressorStreamFactory().createCompressorInputStream(bin);
			try {
				int size = 0;
				while ((size = gin.read(tmp)) > 0) {
					baos.write(tmp, 0, size);
				}
			} catch (Exception e) {

			} finally {
				IOUtils.closeQuietly(gin);
			}
			byte[] ret = baos.toByteArray();
			return ret;
		}
		return new byte[0];
	}


}

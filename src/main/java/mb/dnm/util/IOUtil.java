package mb.dnm.util;

import java.io.*;

/**
 * The type Io util.
 */
public class IOUtil {
    private IOUtil() {}

    /**
     * Get all bytes from an InputStream as byte [ ].
     *
     * @param in the in
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getAllBytes(InputStream in) throws IOException {
        byte[] bytes = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bas = null;

        try {
            bis = new BufferedInputStream(in);
            byte[] buffer = new byte[8 * 1024];
            bas = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(bas);

            int i = 0;
            while((i = bis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, i);
                bos.flush();
            }
            bytes = bas.toByteArray();

        }catch (IOException ie) {
            throw ie;
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (bas != null) {
                bas.close();
            }
        }
        return bytes;
    }
}

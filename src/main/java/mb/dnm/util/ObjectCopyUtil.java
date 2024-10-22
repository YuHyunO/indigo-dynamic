package mb.dnm.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectCopyUtil {

    public static Object deepCopy(Object obj) {
        if (obj == null)
            return null;
        ByteArrayOutputStream byteOut = null;
        ObjectOutputStream out = null;
        ByteArrayInputStream byteIn= null;
        ObjectInputStream in = null;
        try {
            byteOut = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteOut);
            out.writeObject(obj);
            byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            in = new ObjectInputStream(byteIn);
            return in.readObject();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (byteIn != null) {
                    byteIn.close();
                }
                if (byteOut != null) {
                    byteOut.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {}
        }
    }

    public static byte[] objectToByteArray(Object obj) {
        ByteArrayOutputStream byteOut = null;
        ObjectOutputStream out = null;
        try {
            byteOut = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteOut);
            out.writeObject(obj);
            return byteOut.toByteArray();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (byteOut != null) {
                    byteOut.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {}
        }
    }

    public static <T> T bytesToObject(byte[] bytes, Class type) {
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            Object obj = ois.readObject();
            return (T) type.cast(obj);
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (Exception e) {}
        }
    }

}

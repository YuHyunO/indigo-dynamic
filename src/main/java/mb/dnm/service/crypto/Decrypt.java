package mb.dnm.service.crypto;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.crypto.ARIACipher;
import mb.dnm.access.crypto.CryptoType;
import mb.dnm.access.crypto.Seed128Cipher;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.util.GZipUtils;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;

import java.io.*;
import java.nio.file.Files;

@Slf4j
@Setter
public class Decrypt extends ParameterAssignableService {
    private String key = "indigo-dynamic-7";

    /**
     * NONE, SEED128, ARIA128 , JASYPT
     */
    private CryptoType cryptoType = CryptoType.NONE;
    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "Decrypt service must have the input parameter");
        }

        if (getOutput() == null)
            return;
        
        Object inputVal = getInputValue(ctx);
        byte[] decBytes;
        
        switch (cryptoType) {

            case ARIA128: {
                byte[] dataObj = (byte[]) inputVal;
                ByteArrayInputStream bis = null;

                try {
                    ARIACipher aria128 = new ARIACipher();
                    aria128.setPassword(key);
                    decBytes = aria128.decrypt(GZipUtils.gunzip(dataObj));
                    bis = new ByteArrayInputStream(decBytes);
                    ObjectInput in = new ObjectInputStream(bis);

                    setOutputValue(ctx, in.readObject());
                } finally {
                    bis.close();
                }
            }
            break;

            case JASYPT: {
                byte[] dataObj = (byte[]) inputVal;

                ByteArrayInputStream bis = null;

                try {
                    StandardPBEByteEncryptor jasypt = new StandardPBEByteEncryptor();
                    jasypt.setAlgorithm("PBEWithSHA1AndDESede");
                    jasypt.setPassword(key);
                    // decBytes = jasypt.decrypt(GZipUtils.gunzip(dataObj));
                    decBytes = GZipUtils.gunzip(dataObj);
                    bis = new ByteArrayInputStream(decBytes);
                    ObjectInput in = new ObjectInputStream(bis);

                    setOutputValue(ctx, in.readObject());
                } finally {
                    bis.close();
                }
            }
            break;

            case SEED128: {
                byte[] dataObj = (byte[]) inputVal;

                ByteArrayInputStream bis = null;

                try {
                    decBytes = Seed128Cipher.decrypt(GZipUtils.gunzip(dataObj), key.getBytes());
                    bis = new ByteArrayInputStream(decBytes);
                    ObjectInput in = new ObjectInputStream(bis);

                    setOutputValue(ctx, in.readObject());
                } finally {
                    bis.close();
                }
            }
            break;

            default:

                break;
        }
    }

    public void setKey(File keyLoc) throws IOException {
        this.key = new String(Files.readAllBytes(keyLoc.toPath()));
    }
}

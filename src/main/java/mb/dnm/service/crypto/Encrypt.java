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
public class Encrypt extends ParameterAssignableService implements Serializable {
    private static final long serialVersionUID = -8617365270446006298L;
    private String key = "indigo-dynamic-7";

    /**
     * NONE, SEED128, ARIA128 , JASYPT
     */
     private CryptoType cryptoType = CryptoType.NONE;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "Encrypt service must have the input parameter");
        }

        if (getOutput() == null)
            return;

        Object inputVal = getInputValue(ctx);
        byte[] encBytes = null;

        switch (cryptoType) {

            case ARIA128: {
                ByteArrayOutputStream bos = null;
                try {
                    bos = new ByteArrayOutputStream();
                    ObjectOutput out = new ObjectOutputStream(bos);
                    out.writeObject(inputVal);
                    ARIACipher aria128 = new ARIACipher();
                    aria128.setPassword(key);
                    encBytes = aria128.encrypt(bos.toByteArray());
                    setOutputValue(ctx, GZipUtils.gzip(encBytes));
                } finally {
                    bos.close();
                }
            }
            break;

            case JASYPT: {
                ByteArrayOutputStream bos = null;

                try {
                    bos = new ByteArrayOutputStream();
                    ObjectOutput out = new ObjectOutputStream(bos);
                    out.writeObject(inputVal);

                    StandardPBEByteEncryptor jasypt = new StandardPBEByteEncryptor();

                    jasypt.setAlgorithm("PBEWithSHA1AndDESede");
                    jasypt.setPassword(key);
                    setOutputValue(ctx, GZipUtils.gzip(bos.toByteArray()));
                } finally {
                    bos.close();
                }
            }
            break;

            case SEED128: {
                ByteArrayOutputStream bos = null;

                try {
                    bos = new ByteArrayOutputStream();
                    ObjectOutput out = new ObjectOutputStream(bos);
                    out.writeObject(inputVal);

                    encBytes = Seed128Cipher.encrypt(bos.toByteArray(), key.getBytes());
                    setOutputValue(ctx, GZipUtils.gzip(encBytes));
                } finally {
                    bos.close();
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

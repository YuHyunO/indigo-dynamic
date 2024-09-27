package mb.dnm.service.crypto;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.crypto.ARIACipher;
import mb.dnm.access.crypto.CryptoType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

@Slf4j
@Setter
public class Encrypt extends ParameterAssignableService {
    private String key = "indigo-dynamic-7";

    /**
     * NONE, SEED128, ARIA128 , JASYPT
     */
     private CryptoType cryptoType = CryptoType.NONE;

    @Override
    public void process(ServiceContext ctx) throws Throwable {

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
                    onSignalResult.setDataObj(GZipUtils.gzip(encBytes));
                } finally {
                    bos.close();
                }
            }
            break;

            case JASYPT: {
                Object inputVal = onSignalResult.getDataObj();

                ByteArrayOutputStream bos = null;

                try {
                    bos = new ByteArrayOutputStream();
                    ObjectOutput out = new ObjectOutputStream(bos);
                    out.writeObject(inputVal);

                    StandardPBEByteEncryptor jasypt = new StandardPBEByteEncryptor();

                    jasypt.setAlgorithm("PBEWithSHA1AndDESede");
                    jasypt.setPassword(key);
                    // FIXME ��ȣȭ�� ��������ؼ� ���� �ʿ�.
                    // encBytes = jasypt.encrypt(bos.toByteArray());
                    onSignalResult.setDataObj(GZipUtils.gzip(bos.toByteArray()));
                } finally {
                    bos.close();
                }
            }
            break;

            case SEED128: {
                Object inputVal = onSignalResult.getDataObj();

                ByteArrayOutputStream bos = null;

                try {
                    bos = new ByteArrayOutputStream();
                    ObjectOutput out = new ObjectOutputStream(bos);
                    out.writeObject(inputVal);

                    encBytes = Seed128Cipher.encrypt(bos.toByteArray(), key.getBytes());
                    onSignalResult.setDataObj(GZipUtils.gzip(encBytes));
                } finally {
                    bos.close();
                }
            }
            break;

            default:

                break;
        }
    }
}

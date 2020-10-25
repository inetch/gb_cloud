package gb.cloud.common.password;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Password {
    private static byte[] static_salt = {12, 56, 89, 65, 124, 56, 55, 10, 02, 89, 41};

    public static byte[] getHash(String pass) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return getHash(pass, static_salt);
    }

    public static byte[] getHash(String pass, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 128);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }
}

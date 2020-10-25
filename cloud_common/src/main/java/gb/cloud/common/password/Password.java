package gb.cloud.common.password;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class Password {
    private final static byte[] static_salt = {12, 56, 89, 65, 124, 56, 55, 10, 02, 89, 41};

    public static String getHash(String pass) {
        try {
            return new String(getHash(pass, static_salt));
        }catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println(e);
        }
        return pass;
    }

    public static byte[] getHash(String pass, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 128);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }
}

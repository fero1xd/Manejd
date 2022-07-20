package net.fero.manejd.utils;


import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;


public class Hashing {

    private final static int iterations = 1000000;
    private final static int keyLength = 256;
    static byte[] salt = "thisisatestsalteaxwd".getBytes();

    static Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance("AES");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public static String computeMasterKey(String mp) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(mp.toCharArray(), salt, iterations, keyLength);
        SecretKey originalKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return keyToString(originalKey);
    }

    public static String aesEncrypt(String password, String masterKey) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, convertStringToSecretKey(masterKey));

        byte[] utf = cipher.doFinal(password.getBytes());
        return Base64.getEncoder().encodeToString(utf);
    }


    public static String aesDecrypt(String b64Encrypted, String masterKey) throws Exception {
        Cipher dec = Cipher.getInstance("AES");

        dec.init(Cipher.DECRYPT_MODE, convertStringToSecretKey(masterKey));

        byte[] decode = Base64.getDecoder().decode(b64Encrypted);

        byte[] bytes = dec.doFinal(decode);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String keyToString(SecretKey key) {
        byte[] encoded = key.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    public static SecretKey convertStringToSecretKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}

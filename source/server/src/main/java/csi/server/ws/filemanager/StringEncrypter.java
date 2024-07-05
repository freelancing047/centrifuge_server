package csi.server.ws.filemanager;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class StringEncrypter {

    Cipher ecipher;
    Cipher dcipher;

    // 8-byte Salt
    // centrifuge > c3ntr1fug3 > c3 13
    byte[] salt = { (byte) 0xC3, (byte) 0x31, (byte) 0x13, (byte) 0x3C, (byte) 0xC3, (byte) 0x31, (byte) 0x13, (byte) 0x3C };
    // Iteration count
    int iterationCount = 19;

    public StringEncrypter(String phrase) {
        try {
            // Create the key
            KeySpec keySpec = new PBEKeySpec(phrase.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);

            ecipher = Cipher.getInstance(key.getAlgorithm());
            dcipher = Cipher.getInstance(key.getAlgorithm());

            // Prepare the parameter to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

            // Create the ciphers
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        }

        catch (java.security.InvalidAlgorithmParameterException e) {
        } catch (java.security.spec.InvalidKeySpecException e) {
        } catch (javax.crypto.NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        }
    }

    public String encrypt(String str) {
        try {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");
            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);

            // Encode bytes to base64 to get a string
            byte[] result = Base64.getEncoder().encode(enc);
            return new String(result);
        } catch (javax.crypto.BadPaddingException e) {
        } catch (javax.crypto.IllegalBlockSizeException e) {
        } catch (java.io.UnsupportedEncodingException e) {
        }

        return null;
    }

    public String decrypt(String str) {
        try {
            // Decode base64 to get bytes
            byte[] dec = Base64.getDecoder().decode(str.getBytes());
            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);

            // Decode using utf-8
            return new String(utf8, "UTF8");
        } catch (javax.crypto.BadPaddingException e) {
        } catch (javax.crypto.IllegalBlockSizeException e) {
        } catch (java.io.UnsupportedEncodingException e) {
        }

        return null;
    }
}

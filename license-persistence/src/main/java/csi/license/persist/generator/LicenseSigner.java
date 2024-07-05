package csi.license.persist.generator;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;

//LicenseSigner signs the actual License instance.  This class is called
//by LicenseGenerator.  This class assumes that a keystore is available in
//the current directory and that the Certificate and private key, to be used
//for signing, are available from the keystore at the alias provided below.
//The keystore must be in the classpath.
public class LicenseSigner {
   public static final String keystore_pwd = "l1cense589";
   public static final String keystore_filename = "centrifuge.keystore";
   public static final String key_alias = "license";
   public static final String key_pwd = "l1cense589";

   private PublicKey publicKey;
   private PrivateKey privateKey;
   private byte[] certBytes;

   // Constructor - will initialize Keystore and load public and private keys.
   public LicenseSigner() {
      try {
         KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         // The keystore needs to be in the classpath
         ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

         try (InputStream stream = contextClassLoader.getResourceAsStream(keystore_filename)) {
            keyStore.load(stream, key_pwd.toCharArray());

            Certificate certificate = keyStore.getCertificate(key_alias);
            publicKey = certificate.getPublicKey();
            certBytes = certificate.getEncoded();

            // get private key
            KeyStore.PrivateKeyEntry pkEntry =
               (KeyStore.PrivateKeyEntry) keyStore.getEntry(key_alias, new KeyStore.PasswordProtection(key_pwd.toCharArray()));
            privateKey = pkEntry.getPrivateKey();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public PublicKey getPublicKey() {
      return publicKey;
   }

   public byte[] getCertificateBytes() {
      return certBytes;
   }

   // Sign the stream using the private key from the keystore
   public byte[] sign(byte[] stream) {
      try {
         Signature sig = Signature.getInstance(privateKey.getAlgorithm());

         sig.initSign(privateKey);
         sig.update(stream, 0, stream.length);
         return sig.sign();
      }
      // TODO log on Exceptions
      // TODO Need to initialize Log4J
      catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }
}

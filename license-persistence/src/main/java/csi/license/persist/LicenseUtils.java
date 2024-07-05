package csi.license.persist;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LicenseUtils {
   public static byte[] intToByteArray(final int integerArg) throws IOException {
      byte[] results = null;

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           DataOutputStream dos = new DataOutputStream(bos)) {
         dos.writeInt(integerArg);
         dos.flush();

         results = bos.toByteArray();
      }
      return results;
   }

   public static byte[] longToByteArray(final long longArg) throws IOException {
      byte[] results = null;

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           DataOutputStream dos = new DataOutputStream(bos)) {
         dos.writeLong(longArg);
         dos.flush();

         results = bos.toByteArray();
      }
      return results;
   }

   public static byte[] serialize(final Object obj) throws IOException {
      byte[] bytes = null;

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(bos)) {
         out.writeObject(obj);

         bytes = bos.toByteArray();
      }
      return bytes;
   }

   public static byte[] createDigest(final byte[] bytes) {
      byte[] results = null;

      try {
         results = MessageDigest.getInstance("SHA").digest(bytes);
      } catch (NoSuchAlgorithmException nsae) {
         //cannot happen
      }
      return results;
   }
}

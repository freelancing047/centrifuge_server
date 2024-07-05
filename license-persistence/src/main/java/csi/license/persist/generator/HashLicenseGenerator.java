package csi.license.persist.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringJoiner;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import csi.license.persist.LicenseUtils;
import csi.license.persist.persistence.AbstractLicensePersistence;

/*
 * A License Generator that uses shorter and more user-friendly license keys.
 *
 * THIS CLASS SHOULD *NOT* SHIP WITH THE PRODUCT
 */
public class HashLicenseGenerator {
   private static final String ENCODED_LICENSE_CHUNK_SEPARATOR = "-";
   private static final int ENCODED_LICENSE_CHUNK_SIZE = 6;

   public String createLicenseFile(final AbstractLicensePersistence licensePersistence) {
      String licenseKey = generateLicenseKey(licensePersistence);

      System.out.println(licenseKey);
      return licenseKey;
   }

   private String generateLicenseKey(final AbstractLicensePersistence licensePersistence) {
      String licenseKey = null;
      byte[] result = null;

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
         licensePersistence.writeBytesToHashStream(bos);
         result = bos.toByteArray();
      } catch (IOException ioe) {
         System.out.println("Exception generating license key: " + ioe.toString());
      }
      if (result != null) {
         // Create 160-bit SHA-1 Digest from serialized license
         byte[] digest = LicenseUtils.createDigest(result);

         ArrayUtils.reverse(result);

         try (ByteArrayOutputStream licenseStream = new ByteArrayOutputStream()) {
            licenseStream.write(digest);
            licenseStream.write(result);

            byte[] key = licenseStream.toByteArray();
            licenseKey = addSeparators(new Base32().encodeAsString(key).replace("=", ""));
         } catch (Exception e) {
            System.out.println("Exception generating license key: " + e.toString());
         }
      }
      return licenseKey;
   }

   private static String addSeparators(final String encodedString) {
      String result = encodedString;

      if (!StringUtils.isEmpty(encodedString)) {
         int length = encodedString.length();

         if (length < ENCODED_LICENSE_CHUNK_SIZE) {
            result += ENCODED_LICENSE_CHUNK_SEPARATOR;
         } else {
            StringJoiner joiner = new StringJoiner(ENCODED_LICENSE_CHUNK_SEPARATOR);
            int chunks = length / ENCODED_LICENSE_CHUNK_SIZE;
            int offset = length % ENCODED_LICENSE_CHUNK_SIZE;

            if (offset != 0) {
               joiner.add(encodedString.substring(0, offset));
            }
            for (int i = 0; i < chunks; i++) {
               int start = (i * ENCODED_LICENSE_CHUNK_SIZE) + offset;

               joiner.add(encodedString.substring(start, start + ENCODED_LICENSE_CHUNK_SIZE));
            }
            result = joiner.toString();
         }
      }
      return result;
   }

   public static String removeSeparators(final String encodedString) {
      return encodedString.replace(ENCODED_LICENSE_CHUNK_SEPARATOR, "");
   }
}

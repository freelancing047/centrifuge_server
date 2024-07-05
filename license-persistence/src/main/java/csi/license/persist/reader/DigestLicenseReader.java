package csi.license.persist.reader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import csi.license.LicenseException;
import csi.license.persist.generator.HashLicenseGenerator;
import csi.license.persist.persistence.AbstractLicensePersistence;
import csi.license.persist.persistence.AbstractLicensePersistenceFactory;
import csi.license.persist.persistence.LicensePersistenceV1;
import csi.license.persist.persistence.LicensePersistenceV2;

public class DigestLicenseReader implements LicenseReader {
   private static final int DIGEST_LENGTH = 20;

   private static Collection<AbstractLicensePersistenceFactory<?>> licenseFactories;

   static {
      licenseFactories = Arrays.asList(new LicensePersistenceV1().new LicensePersistenceV1Factory(),
                                       new LicensePersistenceV2().new LicensePersistenceV2Factory());
   }

   @Override
   public AbstractLicensePersistence read(final InputStream stream) throws LicenseException {
      AbstractLicensePersistence license = null;

      try {
         String key = IOUtils.toString(stream, "UTF-8");

         if (key != null) {
            byte[] data = new Base32().decode(HashLicenseGenerator.removeSeparators(key.trim()));
            byte[] digest = new byte[DIGEST_LENGTH];
            byte[] licenseData = new byte[data.length - digest.length];

            System.arraycopy(data, 0, digest, 0, digest.length);
            System.arraycopy(data, digest.length, licenseData, 0, licenseData.length);
            ArrayUtils.reverse(licenseData);

            if (verifyDigest(digest, licenseData)) {
               for (AbstractLicensePersistenceFactory<?> licensePersistenceFactory : licenseFactories) {
                  try (ByteArrayInputStream bis = new ByteArrayInputStream(licenseData)) {
                     license = licensePersistenceFactory.create(bis);

                     if (license != null) {
                        break;
                     }
                  } catch (Exception e) {
                     //Failure to create may just mean wrong kind of license
                  }
               }
            }
         }
      } catch (Exception e) {
         throw new LicenseException(e);
      }
      return license;
   }

   private static boolean verifyDigest(final byte[] digest, final byte[] licenseData) throws GeneralSecurityException {
      return Arrays.equals(digest, MessageDigest.getInstance("SHA").digest(licenseData));
   }
}

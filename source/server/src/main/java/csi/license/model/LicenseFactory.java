package csi.license.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.license.LicenseConstants;
import csi.license.persist.persistence.AbstractLicensePersistence;
import csi.license.persist.persistence.LicensePersistenceV1;
import csi.license.persist.persistence.LicensePersistenceV2;
import csi.license.persist.reader.DigestLicenseReader;
import csi.license.persist.reader.LicenseReader;
import csi.server.util.Version;

public class LicenseFactory {
   private static final Logger LOG = LogManager.getLogger(LicenseFactory.class);

   private static Collection<LicenseReader> readers =
      new ArrayList<LicenseReader>(Arrays.asList(new DigestLicenseReader()/*,
                                                 new SignedLicenseReader()*/));

   private LicenseFactory() {
   }

   public static AbstractLicense create() {
      AbstractLicense license = null;

      if (hasLicenseFile()) {
         byte[] rawBytes = null;

         try (FileReader fileReader = new FileReader(LicenseConstants.LICENSE_FILENAME);
              BufferedReader inFile = new BufferedReader(fileReader)) {
            rawBytes = inFile.readLine().getBytes("UTF-8");
         } catch (IOException ioe) {
            LOG.error("The Centrifuge license file could not be processed.");
            System.exit(-1);
         }
         for (LicenseReader reader: readers) {
            try (InputStream bis = new ByteArrayInputStream(rawBytes)) {
               AbstractLicensePersistence persistenceLicense = reader.read(bis);

               if (persistenceLicense != null) {
                  license = createFromPersistence(persistenceLicense);
                  break;
               }
            } catch (Exception e) {
               LOG.info("Reader not effective: {}", () -> reader.getClass().getSimpleName());
            }
         }
         if (license == null) {
            LOG.error("The Centrifuge license file is not valid.");
            System.exit(-1);
         }
      } else {
         license = new NamedLicense(
                      new LicensePersistenceV2("Free",  //customer
                                                Version.getMajorVersion(),
                                                Version.getMinorVersion(),
                                                0,      //versionLabel
                                                1,      //userCount
                                                false,  //internal
                                                false,  //expiring
                                                false,  //nodeLock
                                                false,  //concurrent = true, named = false
                                                ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),  //start
                                                ZonedDateTime.of(3000, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()))); //end
      }
      return license;
   }

   public static boolean hasLicenseFile() {
      return new File(LicenseConstants.LICENSE_FILENAME).exists();
   }

   private static AbstractLicense createFromPersistence(final AbstractLicensePersistence persistenceLicense) {
      AbstractLicense license = null;

      if (persistenceLicense instanceof LicensePersistenceV1) {
         license = new NamedLicense((LicensePersistenceV1) persistenceLicense);
      } else if (persistenceLicense instanceof LicensePersistenceV2) {
         license = (((LicensePersistenceV2) persistenceLicense).isConcurrent())
                      ? new ConcurrentLicense((LicensePersistenceV2) persistenceLicense)
                      : new NamedLicense((LicensePersistenceV2) persistenceLicense);
      }
      return license;
   }
}

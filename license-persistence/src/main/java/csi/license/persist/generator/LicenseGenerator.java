package csi.license.persist.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

import csi.license.LicenseConstants;
import csi.license.persist.LicenseUtils;
import csi.license.persist.persistence.License;
import csi.license.persist.persistence.LicensePersistenceV2;

//import java.security.MessageDigest;

// LicenseGenerator is responsible for creating a license file.  The license file
// contains a base64 encoded string.  The string is a serialized
// LicensePayload instance that has been encoded.  The LicensePayload consists
// of three items.
// 	1.  The serialized license instance
// 	2.  A digital signature of the serialized license
// 	3.  The public certificate that can be used to verify the signature
//

public class LicenseGenerator {
   @Deprecated
   private String generateLicenseKey(final License license) throws IOException {
      LicenseSigner signer = new LicenseSigner();
      byte[] serializedLicense = LicenseUtils.serialize(license);
      byte[] signature = signer.sign(serializedLicense);
      byte[] certBytes = signer.getCertificateBytes();
      LicensePayload payload = new LicensePayload(certBytes, signature, serializedLicense);

      return new String(Base64.getEncoder().encode(LicenseUtils.serialize(payload)));
   }

   @Deprecated
   public boolean createLicenseFile(final License license) throws IOException {
      boolean result = false;
      String licString = generateLicenseKey(license);

      try (FileWriter fileWriter = new FileWriter(LicenseConstants.LICENSE_FILENAME);
           BufferedWriter out = new BufferedWriter(fileWriter)) {
         out.write(licString);
         result = true;
      }
      return result;
   }

   private String generateLicenseKey(final LicensePersistenceV2 license) throws IOException {
      LicenseSigner signer = new LicenseSigner();
      byte[] serializedLicense = LicenseUtils.serialize(license);
      byte[] signature = signer.sign(serializedLicense);
      byte[] certBytes = signer.getCertificateBytes();
      LicensePayload payload = new LicensePayload(certBytes, signature, serializedLicense);

      return new String(Base64.getEncoder().encode(LicenseUtils.serialize(payload)));
   }

   public boolean createLicenseFile(final LicensePersistenceV2 license) throws IOException {
      boolean result = false;
      String licString = generateLicenseKey(license);

      try (FileWriter fileWriter = new FileWriter(LicenseConstants.LICENSE_FILENAME);
           BufferedWriter out = new BufferedWriter(fileWriter)) {
         out.write(licString);
         result = true;
      }
      return result;
   }
}

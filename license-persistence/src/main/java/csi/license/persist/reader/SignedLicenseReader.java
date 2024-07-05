package csi.license.persist.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;

import org.apache.commons.io.IOUtils;

import csi.license.LicenseException;
import csi.license.persist.generator.LicensePayload;
import csi.license.persist.persistence.AbstractLicensePersistence;
import csi.license.persist.persistence.License;
import csi.license.persist.persistence.LicensePersistenceV1;
import csi.license.persist.persistence.LicensePersistenceV2;

public class SignedLicenseReader implements LicenseReader {
   public class SignedObjectInputStream extends ObjectInputStream {
      protected SignedObjectInputStream() throws IOException, SecurityException {
         super();
      }

      public SignedObjectInputStream(final InputStream istream) throws IOException {
         super(istream);
         enableResolveObject(true);
      }

      @Override
      protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
         ObjectStreamClass descriptor = super.readClassDescriptor();

         if (descriptor != null) {
            String name = descriptor.getName();

            if ((name != null) && name.startsWith("centrifuge.license")) {
               String replace = name.replace("centrifuge.license", "csi.license");

               descriptor = ObjectStreamClass.lookup(this.getClass().getClassLoader().loadClass(replace));
            }
         }
         return descriptor;
      }
   }

   @Override
   public AbstractLicensePersistence read(final InputStream stream) throws LicenseException {
      AbstractLicensePersistence license = null;

      try {
         byte[] image = IOUtils.toByteArray(stream);
         byte[] data = Base64.getDecoder().decode(image);
         LicensePayload payload = null;

         try (ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
              ObjectInputStream ois = new SignedObjectInputStream(dataStream)) {
            payload = (LicensePayload) ois.readObject();
         }
         if (isValidSignature(payload)) {
            try (ByteArrayInputStream dataStream = new ByteArrayInputStream(payload.getLic());
                 ObjectInputStream ois = new SignedObjectInputStream(dataStream)) {
               Object obj = ois.readObject();

               if (obj instanceof LicensePersistenceV1) {
                  license = (LicensePersistenceV1) ois.readObject();
               } else  if (obj instanceof LicensePersistenceV2) {
                  license = (LicensePersistenceV2) ois.readObject();
               } else  if (obj instanceof License) {
                  license = LicensePersistenceV1.convertLegacy((License) ois.readObject());
               }
            }
         }
      } catch (Throwable t) {
         throw new LicenseException(t);
      }
      return license;
   }

   private static boolean isValidSignature(final LicensePayload payload) throws GeneralSecurityException {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      Certificate certificate = cf.generateCertificate(new ByteArrayInputStream(payload.getCert()));
      PublicKey key = certificate.getPublicKey();

      // verify that the cert was signed with the private key paired with this public key.
      certificate.verify(key);

      Signature sigObject = Signature.getInstance(key.getAlgorithm());

      sigObject.initVerify(key);
      sigObject.update(payload.getLic(), 0, payload.getLic().length);
      return sigObject.verify(payload.getSig());
   }
}

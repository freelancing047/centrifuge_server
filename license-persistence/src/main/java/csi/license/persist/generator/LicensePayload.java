package csi.license.persist.generator;

import java.io.Serializable;

// A LicensePayload is the class that actually gets serialized out to
// a license file.  It consists of the three byte arrays described in the
// comments for LicenseGenerator.
@SuppressWarnings("serial")
public class LicensePayload implements Serializable {
   private byte[] cert;
   private byte[] sig;
   private byte[] lic;

   public LicensePayload(byte[] certificate, byte[] signature, byte[] license) {
      cert = new byte[certificate.length];
      sig = new byte[signature.length];
      lic = new byte[license.length];

      System.arraycopy(certificate, 0, cert, 0, certificate.length);
      System.arraycopy(signature, 0, sig, 0, signature.length);
      System.arraycopy(license, 0, lic, 0, license.length);
   }

   public byte[] getCert() {
      return cert;
   }
   public byte[] getSig() {
      return sig;
   }
   public byte[] getLic() {
      return lic;
   }
}

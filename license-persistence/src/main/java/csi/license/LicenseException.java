package csi.license;

public class LicenseException extends Exception {
   private static final long serialVersionUID = -163165499666312950L;

   public LicenseException() {
      super();
   }

   public LicenseException(final String message) {
      super(message);
   }

   public LicenseException(final Throwable cause) {
      super(cause);
   }

   public LicenseException(final String message, final Throwable cause) {
      super(message, cause);
   }
}

package csi.license.persist.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import csi.license.persist.LicenseUtils;

public class LicensePersistenceV1 extends AbstractLicensePersistence {
   private static final long serialVersionUID = -2118658747622750389L;

   private static final byte VERSION_V1 = 0x0;

   private Date expirationDate;
   private String asString;

   public class LicensePersistenceV1Factory extends AbstractLicensePersistenceFactory<LicensePersistenceV1> {
      public LicensePersistenceV1 create(final ByteArrayInputStream bis) throws IOException {
         return readBytesFromHashStream(bis);
      }
   }

   public LicensePersistenceV1() {
   }

   public LicensePersistenceV1(final String customer, final int versionMajor, final int versionMinor,
                               final int versionLabel, final int userCount, final boolean internal,
                               final boolean expiring, final boolean nodeLock, final Date expirationDate) {
      super(customer, versionMajor, versionMinor, versionLabel, userCount, internal, expiring, nodeLock);
      this.expirationDate = expirationDate;
      setAsString();
   }

   public LicensePersistenceV1 readBytesFromHashStream(final ByteArrayInputStream bis) throws IOException {
      LicensePersistenceV1 licensePersistenceV1 = null;

      try (DataInputStream dis = new DataInputStream(bis)) {
         int seed = dis.readInt();
         int seatCount = dis.readInt() - seed;
         long millis = dis.readLong();
         boolean expiring = (dis.readByte() == 0x1);
         int major = dis.readInt();
         int minor = dis.readInt();
         /*long customer =*/ dis.readLong();
         byte version = dis.readByte();

         if (version == VERSION_V1) {
            licensePersistenceV1 =
               new LicensePersistenceV1("", major, minor, 0, seatCount, false, expiring, false, new Date(millis));
         }
      } catch (Exception e) {
      }
      return licensePersistenceV1;
   }

   public void writeBytesToHashStream(final ByteArrayOutputStream bos) throws IOException {
      int seed = GENERATOR.nextInt();

      bos.write(LicenseUtils.intToByteArray(seed));
      bos.write(LicenseUtils.intToByteArray(getUserCount() + seed));
      bos.write(LicenseUtils.longToByteArray(expirationDate.getTime()));
      bos.write((byte) (isExpiring() ? 1 : 0));
      bos.write(LicenseUtils.intToByteArray(getVersionMajor()));
      bos.write(LicenseUtils.intToByteArray(getVersionMinor()));
      bos.write(LicenseUtils.longToByteArray(32));  //TODO: customer?
      bos.write(VERSION_V1);  //TODO
      bos.write(0x0);  //TODO
   }

   public static LicensePersistenceV1 convertLegacy(final License license) {
      return new LicensePersistenceV1(license.get_customer(), license.get_ver_major(), license.get_ver_minor(),
                                      license.get_ver_label(), license.get_user_count(), license.get_internal(),
                                      license.is_expiring(), license.get_node_lock(), license.get_expiration_date());
   }

   public Date getExpirationDate() {
      return expirationDate;
   }

   public void setExpirationDate(final Date expirationDate) {
      this.expirationDate = expirationDate;
   }

   private void setAsString() {
      asString = new StringBuilder(super.toString())
                           .append(", expirationDate=").append(expirationDate)
                           .append("]").toString();
   }

   @Override
   public String toString() {
      return asString;
   }
}

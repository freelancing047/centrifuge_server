package csi.license.persist.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import csi.license.persist.LicenseUtils;

public class LicensePersistenceV2 extends AbstractLicensePersistence {
   private static final long serialVersionUID = 2069304217325832809L;

   private static final byte VERSION_V2 = 0x1;

   private boolean concurrent;
   private ZonedDateTime startDateTime;
   private ZonedDateTime expirationDateTime;
   private String asString;

   public class LicensePersistenceV2Factory extends AbstractLicensePersistenceFactory<LicensePersistenceV2> {
      public LicensePersistenceV2 create(final ByteArrayInputStream bis) throws IOException {
         return readBytesFromHashStream(bis);
      }
   }

   public LicensePersistenceV2() {
   }

   public LicensePersistenceV2(final String customer, final int versionMajor, final int versionMinor,
                               final int versionLabel, final int userCount, final boolean internal,
                               final boolean expiring, final boolean nodeLock, final boolean concurrent,
                               final ZonedDateTime startDateTime, final ZonedDateTime expirationDateTime) {
      super(customer, versionMajor, versionMinor, versionLabel, userCount, internal, expiring, nodeLock);
      this.concurrent = concurrent;
      this.startDateTime = startDateTime;
      this.expirationDateTime = expirationDateTime;
      setAsString();
   }

   public LicensePersistenceV2 readBytesFromHashStream(final ByteArrayInputStream bis) throws IOException {
      LicensePersistenceV2 licensePersistenceV2 = null;

      try (DataInputStream dis = new DataInputStream(bis)) {
         int seed = dis.readInt();
         int seatCount = dis.readInt() - seed;
         boolean expiring = (dis.readByte() == 0x1);
         int major = dis.readInt();
         int minor = dis.readInt();
         boolean concurrent = (dis.readByte() == 0x1);
         Instant fromMillis = Instant.ofEpochMilli(dis.readLong());
         Instant toMillis = Instant.ofEpochMilli(dis.readLong());
         byte version = dis.readByte();

         if (version == VERSION_V2) {
            licensePersistenceV2 =
               new LicensePersistenceV2("", major, minor, 0, seatCount, false, expiring, false, concurrent,
                                        ZonedDateTime.ofInstant(fromMillis, ZoneId.systemDefault()),
                                        ZonedDateTime.ofInstant(toMillis, ZoneId.systemDefault()));
         }
      } catch (Exception e) {
      }
      return licensePersistenceV2;
   }

   public void writeBytesToHashStream(final ByteArrayOutputStream bos) throws IOException {
      int seed = GENERATOR.nextInt();

      bos.write(LicenseUtils.intToByteArray(seed));
      bos.write(LicenseUtils.intToByteArray(getUserCount() + seed));
      bos.write((byte) (isExpiring() ? 1 : 0));
      bos.write(LicenseUtils.intToByteArray(getVersionMajor()));
      bos.write(LicenseUtils.intToByteArray(getVersionMinor()));
      bos.write((byte) (isConcurrent() ? 1 : 0));
      bos.write(LicenseUtils.longToByteArray(getStartDateTime().toInstant().toEpochMilli()));
      bos.write(LicenseUtils.longToByteArray(getExpirationDateTime().toInstant().toEpochMilli()));
      bos.write(VERSION_V2);
   }

   public boolean isConcurrent() {
      return concurrent;
   }
   public ZonedDateTime getStartDateTime() {
      return startDateTime;
   }
   public ZonedDateTime getExpirationDateTime() {
      return expirationDateTime;
   }

   public void setConcurrent(final boolean concurrent) {
      this.concurrent = concurrent;
      setAsString();
   }
   public void setStartDateTime(final ZonedDateTime startDateTime) {
      this.startDateTime = startDateTime;
      setAsString();
   }
   public void setExpirationDateTime(final ZonedDateTime expirationDateTime) {
      this.expirationDateTime = expirationDateTime;
      setAsString();
   }

   private void setAsString() {
      asString = new StringBuilder(super.toString())
                           .append(", concurrent=").append(concurrent)
                           .append(", startDateTime=").append(startDateTime)
                           .append(", expirationDateTime=").append(expirationDateTime)
                           .append("]").toString();
   }

   @Override
   public String toString() {
      return asString;
   }
}

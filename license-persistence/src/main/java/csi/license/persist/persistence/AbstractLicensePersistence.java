package csi.license.persist.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

@SuppressWarnings("serial")
public abstract class AbstractLicensePersistence implements Serializable {
   protected static final Random GENERATOR = new Random();

   private String customer;
   private int versionMajor;
   private int versionMinor;
   private int versionLabel;
   private int userCount;
   private boolean internal;
   private boolean expiring;
   private boolean nodeLock;
   private String asString;

   public AbstractLicensePersistence() {
   }

   public AbstractLicensePersistence(final String customer, final int versionMajor, final int versionMinor,
                                     final int versionLabel, final int userCount, final boolean internal,
                                     final boolean expiring, final boolean nodeLock) {
      this.customer = customer;
      this.versionMajor = versionMajor;
      this.versionMinor = versionMinor;
      this.versionLabel = versionLabel;
      this.userCount = userCount;
      this.internal = internal;
      this.expiring = expiring;
      this.nodeLock = nodeLock;
      setAsString();
   }

   public abstract AbstractLicensePersistence readBytesFromHashStream(final ByteArrayInputStream bis) throws IOException;
   public abstract void writeBytesToHashStream(final ByteArrayOutputStream bos) throws IOException;

   public String getCustomer() {
      return customer;
   }
   public int getVersionMajor() {
      return versionMajor;
   }
   public int getVersionMinor() {
      return versionMinor;
   }
   public int getVersionLabel() {
      return versionLabel;
   }
   public int getUserCount() {
      return userCount;
   }
   public boolean isInternal() {
      return internal;
   }
   public boolean isExpiring() {
      return expiring;
   }
   public boolean isNodeLock() {
      return nodeLock;
   }

   public void setCustomer(final String customer) {
      this.customer = customer;
      setAsString();
   }
   public void setVersionMajor(final int versionMajor) {
      this.versionMajor = versionMajor;
      setAsString();
   }
   public void setVersionMinor(final int versionMinor) {
      this.versionMinor = versionMinor;
      setAsString();
   }
   public void setVersionLabel(final int versionLabel) {
      this.versionLabel = versionLabel;
      setAsString();
   }
   public void setUserCount(final int userCount) {
      this.userCount = userCount;
      setAsString();
   }
   public void setInternal(final boolean internal) {
      this.internal = internal;
      setAsString();
   }
   public void setExpiring(final boolean expiring) {
      this.expiring = expiring;
      setAsString();
   }
   public void setNodeLock(final boolean nodeLock) {
      this.nodeLock = nodeLock;
      setAsString();
   }

   private void setAsString() {
      asString = new StringBuilder("License [customer=").append(customer)
                           .append(", versionMajor=").append(versionMajor)
                           .append(", versionMinor=").append(versionMinor)
                           .append(", versionLabel=").append(versionLabel)
                           .append(", userCount=").append(userCount)
                           .append(", internal=").append(internal)
                           .append(", expiring=").append(expiring)
                           .append(", nodeLock=").append(nodeLock).toString();
   }

   @Override
   public String toString() {
      return asString;
   }
}

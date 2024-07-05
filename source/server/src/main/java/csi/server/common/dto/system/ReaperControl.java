package csi.server.common.dto.system;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import csi.server.common.enumerations.AclResourceType;

/**
 * Created by centrifuge on 7/26/2016.
 */
public class ReaperControl {
   private static final long MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1);

   private AclResourceType resourceType;
   private String remarks;
   private List<String> owners;
   private Integer age;
   private boolean accessAge;

   public AclResourceType getResourceType() {
      return resourceType;
   }

   public void setResourceType(AclResourceType resourceTypeIn) {
      resourceType = resourceTypeIn;
   }

   public String getRemarks() {
      return remarks;
   }

   public void setRemarks(String remarksIn) {
      remarks = remarksIn;
   }

   public String[] getOwnersArray() {
      return ((null != owners) && !owners.isEmpty()) ? owners.toArray(new String[0]) : null;
   }

   public void addOwner(String ownerIn) {
      if ((null != ownerIn) && (0 < ownerIn.length())) {
         if (null == owners) {
            owners = new ArrayList<String>();
         }
         owners.add(ownerIn);
      }
   }

   public List<String> getOwners() {
      return owners;
   }

   public void setOwners(List<String> ownersIn) {
      if ((null != ownersIn) && !ownersIn.isEmpty()) {
         owners = new ArrayList<String>();
         owners.addAll(ownersIn);
      } else {
         owners = null;
      }
   }

   public boolean isAccessAge() {
      return accessAge;
   }

   public void setAccessAge(boolean accessAgeIn) {
      accessAge = accessAgeIn;
   }

   public Integer getAge() {
      return age;
   }

   public void setAge(Integer ageIn) {
      age = ageIn;
   }

   public Date[] getFilter() {
      if (null != age) {
         Date myDate = new Date(((new Date().getTime() / MILLIS_PER_DAY) - age) * MILLIS_PER_DAY);
         return new Date[] { (accessAge) ? null : myDate, (accessAge) ? myDate : null, null };
      }
      return null;
   }
}

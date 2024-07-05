package csi.server.common.model.extension;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ExtensionData extends ModelObject {

   int ordinal;

   protected String name;

   public ExtensionData() {
      super();
   }

   public int getOrdinal() {
      return ordinal;
   }

   public void setOrdinal(int ordinal) {
      this.ordinal = ordinal;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public ExtensionData clone() {

      ExtensionData myClone = new ExtensionData();

      super.cloneComponents(myClone);

      myClone.setName(getName());

      return myClone;
   }
}

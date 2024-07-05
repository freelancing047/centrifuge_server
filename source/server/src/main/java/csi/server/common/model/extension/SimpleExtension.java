package csi.server.common.model.extension;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.FieldDef;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SimpleExtension extends ModelObject {

   int ordinal;

   protected String name;
   protected String description;

   @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
   protected FieldDef fieldRef;

   public SimpleExtension() {
      super();
   }

   public int getOrdinal() {
      return ordinal;
   }

   public void setOrdinal(int ordinal) {
      this.ordinal = ordinal;
   }

   /*
    * (non-Javadoc)
    *
    * @see csi.server.common.model.extension.Extension#getExtensionName()
    */
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   /*
    * (non-Javadoc)
    *
    * @see csi.server.common.model.extension.Extension#getFieldRef()
    */
   public FieldDef getFieldRef() {
      return fieldRef;
   }

   public void setFieldRef(FieldDef fieldRef) {
      this.fieldRef = fieldRef;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T extends ModelObject> SimpleExtension clone(Map<String,T> fieldMapIn) {

      SimpleExtension myClone = new SimpleExtension();

      super.cloneComponents(myClone);

      myClone.setName(getName());
      myClone.setDescription(getDescription());
      myClone.setFieldRef((FieldDef) cloneFromOrToMap(fieldMapIn, (T) getFieldRef(), fieldMapIn));

      return myClone;
   }
}

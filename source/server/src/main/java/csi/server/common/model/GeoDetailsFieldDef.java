package csi.server.common.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GeoDetailsFieldDef extends ModelObject {

   @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
   protected FieldDef field;

   public FieldDef getField() {
      return field;
   }

   public void setField(FieldDef field) {
      this.field = field;
   }

   public GeoDetailsFieldDef() {
      super();
   }

   public GeoDetailsFieldDef(FieldDef field) {
      super();
      this.field = field;
   }
}

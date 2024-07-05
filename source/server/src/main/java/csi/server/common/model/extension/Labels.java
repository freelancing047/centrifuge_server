package csi.server.common.model.extension;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Labels extends SimpleExtension {
   public static final String NAME = "sec-labels";

   public Labels() {
      super();
      this.name = NAME;
      this.description = "Configuration for security labels for a Dataview";
   }

   @ElementCollection
   protected Set<String> defaultValues;

   public Set<String> getDefaultValues() {
      if (defaultValues == null) {
         defaultValues = new HashSet<String>();
      }
      return defaultValues;
   }

   public void setDefaultValues(Set<String> defaultValues) {
      this.defaultValues = (defaultValues == null) ? new HashSet<String>() : defaultValues;
   }
}

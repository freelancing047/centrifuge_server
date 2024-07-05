package csi.server.common.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SpinoffTuple extends ModelObject {

   @OneToMany(cascade = CascadeType.ALL)
   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   public List<SpinoffField> fields;

   public SpinoffTuple() {
      super();
   }
}

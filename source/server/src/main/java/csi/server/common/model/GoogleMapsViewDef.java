package csi.server.common.model;

import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GoogleMapsViewDef extends VisualizationDef {

   public GoogleMapsViewDef() {
      super();
   }

   public GoogleMapsViewDef(String name) {
      super(name);
   }

   @Override
   public Selection getSelection() {
      return NullSelection.instance;
   }

   @Override
   public <T extends ModelObject,S extends ModelObject> VisualizationDef
         clone(Map<String,T> fieldMapIn, Map<String,S> filterMapIn) {

      // log.error("Attempting to clone unsupported visualization type
      // \"GoogleMapsViewDef\"");
      return null;
   }

   @Override
   public <T extends ModelObject,S extends ModelObject> VisualizationDef
         copy(Map<String,T> fieldMapIn, Map<String,S> filterMapIn) {

      // log.error("Attempting to clone unsupported visualization type
      // \"GoogleMapsViewDef\"");
      return null;
   }
}

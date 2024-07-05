package csi.server.business.visualization.graph.base.property;

import java.util.function.Function;

import csi.server.common.model.attribute.AttributeAggregateType;

public class ComputedProperty extends Property {
   protected Function<Property,Double> function;
   protected AttributeAggregateType type;

   public ComputedProperty(String name) {
      super(name);
   }

   /**
    * @return the type
    */
   public AttributeAggregateType getType() {
      return type;
   }

   /**
    * @param type the type to set
    */
   public void setType(AttributeAggregateType type) {
      this.type = type;
   }
}
